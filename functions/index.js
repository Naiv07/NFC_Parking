// Firebase Cloud Function (Node.js)
// Deploy this to Firebase Functions to automatically clean up expired bookings

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

/**
 * Scheduled function that runs every hour to clean up expired bookings
 * 
 * Deploy with: firebase deploy --only functions:cleanupExpiredBookings
 * 
 * This function will:
 * 1. Find all pending/active bookings that have passed their endTime
 * 2. Update their status to 'expired'
 * 3. Log the cleanup results
 */
exports.cleanupExpiredBookings = functions.pubsub
    .schedule('every 1 hours')
    .timeZone('America/Los_Angeles') // Change to your timezone
    .onRun(async (context) => {
        const now = Date.now();
        const batchSize = 500; // Firestore batch limit
        
        console.log('Starting cleanup of expired bookings...');
        
        try {
            // Find expired bookings
            const expiredBookingsSnapshot = await db.collection('bookings')
                .where('status', 'in', ['pending', 'active'])
                .where('endTime', '<', now)
                .limit(batchSize)
                .get();
            
            if (expiredBookingsSnapshot.empty) {
                console.log('No expired bookings found');
                return null;
            }
            
            // Update in batches
            const batch = db.batch();
            let count = 0;
            
            expiredBookingsSnapshot.docs.forEach(doc => {
                batch.update(doc.ref, {
                    status: 'expired',
                    expiredAt: now
                });
                count++;
            });
            
            await batch.commit();
            console.log(`Successfully expired ${count} bookings`);
            
            return { success: true, count };
            
        } catch (error) {
            console.error('Error cleaning up expired bookings:', error);
            return { success: false, error: error.message };
        }
    });

/**
 * HTTP triggered function to manually cleanup expired bookings for a specific location
 * 
 * Call with: POST https://YOUR_REGION-YOUR_PROJECT.cloudfunctions.net/cleanupExpiredBookingsByLocation
 * Body: { "locationId": "location123" }
 */
exports.cleanupExpiredBookingsByLocation = functions.https.onCall(async (data, context) => {
    // Verify authentication
    if (!context.auth) {
        throw new functions.https.HttpsError(
            'unauthenticated',
            'User must be authenticated to cleanup bookings'
        );
    }
    
    const locationId = data.locationId;
    if (!locationId) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'locationId is required'
        );
    }
    
    const now = Date.now();
    const batchSize = 500;
    
    try {
        const expiredBookingsSnapshot = await db.collection('bookings')
            .where('locationId', '==', locationId)
            .where('status', 'in', ['pending', 'active'])
            .where('endTime', '<', now)
            .limit(batchSize)
            .get();
        
        if (expiredBookingsSnapshot.empty) {
            return { success: true, count: 0, message: 'No expired bookings found' };
        }
        
        const batch = db.batch();
        let count = 0;
        
        expiredBookingsSnapshot.docs.forEach(doc => {
            batch.update(doc.ref, {
                status: 'expired',
                expiredAt: now
            });
            count++;
        });
        
        await batch.commit();
        
        return { success: true, count, message: `Expired ${count} bookings` };
        
    } catch (error) {
        console.error('Error cleaning up bookings:', error);
        throw new functions.https.HttpsError('internal', error.message);
    }
});

/**
 * Firestore trigger that automatically expires a booking when its endTime is reached
 * This is more real-time but uses more resources
 * 
 * Optional: Use this instead of scheduled function if you need immediate expiration
 */
exports.autoExpireBooking = functions.firestore
    .document('bookings/{bookingId}')
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();
        
        // Only process if status is pending or active
        if (after.status !== 'pending' && after.status !== 'active') {
            return null;
        }
        
        const now = Date.now();
        
        // Check if booking has expired
        if (after.endTime < now) {
            console.log(`Auto-expiring booking ${context.params.bookingId}`);
            
            return change.after.ref.update({
                status: 'expired',
                expiredAt: now
            });
        }
        
        return null;
    });

/**
 * Archive old bookings to reduce active database size
 * Runs daily and moves bookings older than 30 days to an archive collection
 */
exports.archiveOldBookings = functions.pubsub
    .schedule('every 24 hours')
    .timeZone('America/Los_Angeles')
    .onRun(async (context) => {
        const thirtyDaysAgo = Date.now() - (30 * 24 * 60 * 60 * 1000);
        const batchSize = 500;
        
        console.log('Starting archival of old bookings...');
        
        try {
            const oldBookingsSnapshot = await db.collection('bookings')
                .where('status', 'in', ['completed', 'expired', 'cancelled'])
                .where('endTime', '<', thirtyDaysAgo)
                .limit(batchSize)
                .get();
            
            if (oldBookingsSnapshot.empty) {
                console.log('No old bookings to archive');
                return null;
            }
            
            const batch = db.batch();
            let count = 0;
            
            oldBookingsSnapshot.docs.forEach(doc => {
                // Copy to archive collection
                const archiveRef = db.collection('bookings_archive').doc(doc.id);
                batch.set(archiveRef, {
                    ...doc.data(),
                    archivedAt: Date.now()
                });
                
                // Delete from main collection
                batch.delete(doc.ref);
                count++;
            });
            
            await batch.commit();
            console.log(`Successfully archived ${count} bookings`);
            
            return { success: true, count };
            
        } catch (error) {
            console.error('Error archiving old bookings:', error);
            return { success: false, error: error.message };
        }
    });

/**
 * Send notification when booking is about to expire (15 minutes before)
 * This requires FCM (Firebase Cloud Messaging) to be set up
 */
exports.sendBookingExpiryNotification = functions.pubsub
    .schedule('every 5 minutes')
    .onRun(async (context) => {
        const now = Date.now();
        const fifteenMinutesFromNow = now + (15 * 60 * 1000);
        
        try {
            // Find bookings expiring in the next 15 minutes that haven't been notified
            const expiringBookingsSnapshot = await db.collection('bookings')
                .where('status', '==', 'active')
                .where('endTime', '>', now)
                .where('endTime', '<', fifteenMinutesFromNow)
                .where('expiryNotificationSent', '==', false)
                .get();
            
            if (expiringBookingsSnapshot.empty) {
                return null;
            }
            
            const notifications = [];
            
            for (const doc of expiringBookingsSnapshot.docs) {
                const booking = doc.data();
                
                // Get user's FCM token
                const userDoc = await db.collection('users').doc(booking.userId).get();
                const fcmToken = userDoc.data()?.fcmToken;
                
                if (fcmToken) {
                    const message = {
                        token: fcmToken,
                        notification: {
                            title: 'Parking Expiring Soon',
                            body: `Your parking at ${booking.locationName} expires in 15 minutes`
                        },
                        data: {
                            bookingId: booking.bookingId,
                            type: 'expiring_soon'
                        }
                    };
                    
                    notifications.push(admin.messaging().send(message));
                    
                    // Mark notification as sent
                    await doc.ref.update({ expiryNotificationSent: true });
                }
            }
            
            await Promise.all(notifications);
            console.log(`Sent ${notifications.length} expiry notifications`);
            
            return { success: true, count: notifications.length };
            
        } catch (error) {
            console.error('Error sending notifications:', error);
            return { success: false, error: error.message };
        }
    });