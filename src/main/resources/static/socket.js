
var stompClient = null;
var userRole = null;
var isConnecting = false;

// Store notifications
var notifications = [];

function createConnection(role) {
    
    if(stompClient != null && stompClient.connected){
        console.log('already connected')
        return ; 
    }

    if(isConnecting) {
        console.log('Connection in progress, please wait...');
        return;
    }
    
    isConnecting = true;
    userRole = role;
    
    var socket = new SockJS("http://localhost:8000/billbuddy");
    stompClient = Stomp.over(socket);
    
    // Enable debug mode for troubleshooting
    // stompClient.debug = function(msg) {
    //     console.log('STOMP DEBUG:', msg);
    // };
    
    // send Authorization : <Bearer Token> in header while jwt
    stompClient.connect(
        { 'role': role },
        connectCallBack,
        connectErrorCallBack
    );

    function connectCallBack(frame) {
        console.log("Connection successful:", frame);
        isConnecting = false;

        // Subscribe to user-specific notification queue
        stompClient.subscribe('/user/queue/notify', subscribeCallBack, subscribeErrorCallBack);
        console.log('Subscribed to /user/queue/notify');
    }

    function connectErrorCallBack(error) {
        console.error("Connection error:", error);
        isConnecting = false;
        alert('❌ WebSocket connection failed! Please refresh the page.');
        // Attempt to reconnect after 5 seconds
        setTimeout(() => {
            console.log('Attempting to reconnect...');
            createConnection(role);
        }, 5000);
    }

    function subscribeCallBack(message) {
        if(message.body) {
            console.log('Received notification:', message.body);
            
            try {
                const notification = JSON.parse(message.body);
                console.log('Parsed notification:', notification);
                
                // Handle based on role
                if (role === 'CUSTOMER') {
                    // Customer receives bills from merchant
                    handleCustomerNotification(notification);
                } else if (role === 'MERCHANT') {
                    // Merchant receives responses from customer
                    handleMerchantNotification(notification);
                }
                
            } catch (err) {
                console.error('Failed to parse notification:', err);
            }
        }
    }

    function subscribeErrorCallBack(error) {
        console.error('Subscription error:', error);
        alert('⚠️ Failed to subscribe to notifications. Please refresh the page.');
    }

    // Monitor WebSocket disconnect
    socket.onclose = function(event) {
        console.warn('❌ WebSocket connection closed');
        console.warn('Close event:', {
            code: event.code,
            reason: event.reason,
            wasClean: event.wasClean
        });
        stompClient = null;
        isConnecting = false;
        alert('⚠️ WebSocket disconnected. Attempting to reconnect...');
        setTimeout(() => {
            createConnection(role);
        }, 5000);
    };

    socket.onerror = function(error) {
        console.error('❌ WebSocket error:', error);
        console.error('Error details:', {
            type: error.type,
            target: error.target,
            message: error.message
        });
        isConnecting = false;
    };
}

// ============ CUSTOMER FUNCTIONS ============
function handleCustomerNotification(notification) {
    // Add notification to array
    notifications.push(notification);
    
    // Render notifications for customer
    renderCustomerNotifications();
}

function renderCustomerNotifications() {
    const customerBillsList = document.getElementById('customerBillsList');
    
    if (!customerBillsList) return;
    
    if (notifications.length === 0) {
        customerBillsList.innerHTML = '<div class="text-muted text-center py-3">No bills received yet</div>';
        return;
    }
    
    customerBillsList.innerHTML = notifications.map((notif, index) => {
        // Log each notification to debug
        console.log(`Bill #${index}:`, {
            billId: notif.billId,
            merchantUserName: notif.merchantUserName,
            merchantName: notif.merchantName,
            hasAllFields: !!notif.merchantUserName
        });
        
        return `
        <div class="card mb-2" id="customer-bill-${index}">
            <div class="card-body">
                <div class="fw-semibold">💸 Bill from ${notif.merchantName}</div>
                <div class="text-muted">Bill ID: ${notif.billId}</div>
                <div class="mt-2">Amount: ₹${parseFloat(notif.amount).toFixed(2)}</div>
                <div>Title: ${notif.title}</div>
                <div class="text-muted">Customer: ${notif.customerName}</div>
                <div class="mt-3">
                    <button class="btn btn-success btn-sm" onclick="handleCustomerPay(${index})">
                        Pay ₹${parseFloat(notif.amount).toFixed(2)}
                    </button>
                </div>
            </div>
        </div>
    `}).join('');
}

async function handleCustomerPay(index) {
    const bill = notifications[index];
    if (!bill) {
        console.error('Bill not found at index:', index);
        return;
    }
   
    // Validate merchantUserName EXISTS FIRST
    if (!bill.merchantUserName || bill.merchantUserName.trim() === '') {
        alert(' ERROR: Merchant information is incomplete. Please refresh the page and try again.');
        return;
    }
    
    const payConfirm = confirm(`Pay received bill amount? ₹${parseFloat(bill.amount).toFixed(2)}`);
    if (payConfirm) {
        

        try {
            // Fetch payment details with error handling
            const resp = await fetch(`/initiate/payment/${bill.billId}`, {
                method: 'POST',
            });
            
            // Check if response is successful
            if (!resp.ok) {
                throw new Error(`Payment initiation failed with status ${resp.status}`);
            }

            const data = await resp.json();
            
            console.log('received initiate payment response:', data);
            
            invokePaytmPayment(data);
            
        } catch (err) {
            console.error(' FETCH ERROR - Failed to initiate payment:', err);
            console.error('Error message:', err.error);
            alert(' Payment initiation failed: ' + err.error);
            return;
        }
    }

    function invokePaytmPayment(data) {
        const script = document.createElement('script');

        script.src = "https://securestage.paytmpayments.com/merchantpgpui/checkoutjs/merchants/tXvhzx31620803258735.js" ; 
        script.crossOrigin = "anonymous" ; 
        script.onload = function() {
            var config = {
                    "root": "",
                    "flow": "DEFAULT",
                    "data": {
                    "orderId": data.orderId,
                    "token": data.body.txnToken ,
                    "tokenType": "TXN_TOKEN",
                    "amount": data.amount 
                    },
                   "merchant": {
                        mid :  "tXvhzx31620803258735", 
                        redirect: false 
                    },
                    "handler": {
                            "notifyMerchant": function(eventName,data){
                                console.log("notifyMerchant handler function called");
                                console.log("eventName => ",eventName);
                                console.log("data => ",data);
                               },
                            "transactionStatus": function(data){
                                console.log("transactionStatus handler function called");
                                console.log("data => ",data);  

                                
                                
                                     alert(data.STATUS + " " + data.RESPMSG);

                                    // verify the payment status with paytm
                                    verifyPaymentTransaction(data.ORDERID) ; 

                                window.Paytm.CheckoutJS.close(); // close the checkout paytm window
                            }  
                    }  
                };

                  if(window.Paytm && window.Paytm.CheckoutJS){
                        window.Paytm.CheckoutJS.onLoad(function excecuteAfterCompleteLoad() {
                            // initialze configuration using init method
                            window.Paytm.CheckoutJS.init(config).then(function onSuccess() {
                                // after successfully updating configuration, invoke JS Checkout
                                window.Paytm.CheckoutJS.invoke();
                            }).catch(function onError(error){
                            console.log("error => ",error);
                            });
                        });
                    }
    
            }
            
             document.body.appendChild(script);
          
            }

         async function verifyPaymentTransaction(orderId) {

             const result  = await fetch(`/verify/payment/${orderId}`, {
                method: 'POST',
            }).then(resp => {
                if (!resp.ok) {
                    throw new Error(`Payment verification failed with status ${resp.status}`);
                }
                return resp.json();
             }).then(data => {

                console.log('Payment verification response:', data);

                sendPaymentAction(bill, index  , data);

              
            }).catch(err => {
                console.error('Payment verification error:', err);
                 alert('Payment verification failed: ' + err.error);
            });
         }   
}

// Helper function to send payment confirmation to merchant after successful Paytm payment
function sendPaymentAction(bill, index , data) {
    // Send payment confirmation back to merchant through WebSocket
    const paymentResponse = {
        billId: bill.billId,
        customerId: bill.customerId,
        customerName: bill.customerName,
        merchantId: bill.merchantId,
        merchantName: bill.merchantName,
        title: bill.title,
        status: data.body.resultInfo.resultStatus,
        timestamp: new Date().toISOString(),
        amount: bill.amount
    };

    // Send to merchant's specific queue
    if (typeof stompClient !== "undefined" && stompClient !== null && stompClient.connected) {
        try {
            // Validate payload structure
            if (typeof paymentResponse !== 'object' || paymentResponse === null) {
                throw new Error('Invalid payment response object');
            }
            

            const finalPayload = {
                paymentResponse : paymentResponse,
                paymentData : data
            }
            // Ensure we're sending a proper JSON string, not nested arrays
            const jsonString = JSON.stringify(finalPayload);


            // Send payment response to merchant
            stompClient.send(
                "/app/bill.response",
                {
                    merchantUserName: bill.merchantUserName , 
                },
                jsonString
            );
            
            console.log('✅ Message sent to /app/bill.response endpoint');
            console.log('⏳ Waiting for server processing...');
            
            // Wait a moment before removing (let server process it)
            setTimeout(() => { 
                notifications.splice(index, 1);
                renderCustomerNotifications();
                alert('Payment status sent!');
                if (typeof window.refreshCustomerBills === "function") {
                    window.refreshCustomerBills();
                }
            }, 1000);
            
        } catch (err) {
            console.error('❌ SEND ERROR - Failed to send payment response:', err);
            console.error('Error message:', err.error);
            console.error('Error stack:', err.stack);
            alert('❌ Failed to send payment confirmation: ' + err.error);
        }
    } else {
        console.error('❌ WEBSOCKET NOT CONNECTED!');
        console.log('Details:', {
            clientExists: typeof stompClient !== "undefined",
            clientNull: stompClient === null,
            connected: stompClient?.connected
        });
        alert('❌ WebSocket connection is not active. Please refresh the page.');
    }
}

// function handleCustomerDecline(index) {
//     const bill = notifications[index];
//     if (!bill) {
//         console.error('❌ Bill not found at index:', index);
//         return;
//     }
    
//     console.log('Full bill object:', JSON.stringify(bill, null, 2));
//     console.log('merchantUserName value:', bill.merchantUserName);
    
//     const confirmed = confirm('Are you sure you want to decline this bill?');
//     if (confirmed) {
//         console.log('✅ User confirmed decline');
//         alert('Declining bill...');
        
//         // CHECK: Validate merchantUserName exists
//         if (!bill.merchantUserName || bill.merchantUserName.trim() === '') {
//             console.error('❌ CRITICAL: merchantUserName is missing or empty!');
//             console.error('Bill object:', bill);
//             alert('⚠️ ERROR: Merchant information is incomplete. Please refresh the page and try again.');
//             return;
//         }
        
//         console.log('🚀 Creating decline response object');
        
//         // Send decline confirmation back to merchant through WebSocket
//         // IMPORTANT: Include merchantUserName in payload for routing
//         const declineResponse = {
//             billId: bill.billId,
//             customerId: bill.customerId,
//             customerName: bill.customerName,
//             merchantId: bill.merchantId,
//             merchantName: bill.merchantName,
//             title: bill.title,
//             status: 'DECLINED',
//             timestamp: new Date().toISOString(),
//             amount: bill.amount
//         };

//         console.log('📤 Decline Response Object Created:');
//         console.log(JSON.stringify(declineResponse, null, 2));

//         // Verify WebSocket connection BEFORE sending
//         console.log('🔌 WebSocket Status Check:');
//         console.log('- stompClient exists:', typeof stompClient !== "undefined");
//         console.log('- stompClient is null:', stompClient === null);
//         console.log('- stompClient.connected:', stompClient?.connected);
        
//         // Send to merchant's specific queue
//         if (typeof stompClient !== "undefined" && stompClient !== null && stompClient.connected) {
//             try {
//                 console.log('📡 Attempting to send decline response...');
                
//                 // Validate payload structure
//                 if (typeof declineResponse !== 'object' || declineResponse === null) {
//                     throw new Error('Invalid decline response object');
//                 }
                
//                 // Ensure we're sending a proper JSON string, not nested arrays
//                 const jsonString = JSON.stringify(declineResponse);
//                 console.log('📋 JSON String to send:', jsonString);
//                 console.log('✅ Valid JSON: ', jsonString[0] === '{' && jsonString[jsonString.length - 1] === '}');
                
//                 // Method: Send WITHOUT header, include merchantUserName in payload
//                 // The backend will extract it from the payload
//                 stompClient.send(
//                     "/app/bill.response",
//                     {
//                         merchantUserName: bill.merchantUserName  // Include in header for backend routing (optional if backend extracts from payload)
//                     },  // No custom headers - just empty object
//                     jsonString  // Send the stringified object
//                 );
                
//                 console.log('✅ Message sent to /app/bill.response endpoint');
//                 console.log('⏳ Waiting for server processing...');
                
//             } catch (err) {
//                 console.error('❌ SEND ERROR - Failed to send decline response:', err);
//                 console.error('Error message:', err.error);
//                 console.error('Error stack:', err.stack);
//                 alert('❌ Failed to send decline confirmation: ' + err.error);
//                 return;
//             }
//         } else {
//             console.error('❌ WEBSOCKET NOT CONNECTED!');
//             console.log('Details:', {
//                 clientExists: typeof stompClient !== "undefined",
//                 clientNull: stompClient === null,
//                 connected: stompClient?.connected
//             });
//             alert('❌ WebSocket connection is not active. Please refresh the page.');
//             return;
//         }
        
//         console.log('⏳ Removing bill from list after 1 second...');
        
//         // Wait a moment before removing (let server process it)
//         setTimeout(() => {
//             notifications.splice(index, 1);
//             renderCustomerNotifications();
//             console.log('✅ Decline processed: Bill removed from list');
//         }, 1000);
        
//         console.log('✅ DECLINE FLOW COMPLETE');
//         alert('✅ Bill declined and notification sent!');
//     }
// }

// ============ MERCHANT FUNCTIONS ============


function handleMerchantNotification(notification) {
     
     const paymentData = notification.paymentData ; 
     const paymentResponse = notification.paymentResponse ;

     // an endpoint for saving the paymentData in database
        savePaymentDatabyMerchant(paymentData , paymentResponse.billId) ;
     // an endpoint for updating the bill status in database
        updateBillByMerchant(paymentResponse.billId , paymentResponse.status) ;
    
    // Verify addCustomerResponse function exists
    if (typeof addCustomerResponse === 'function') {
        try {
            addCustomerResponse(paymentResponse);
        } catch (err) {
            console.error('Error details:', err.error);
            console.error('Stack:', err.stack);
        }
    } else {
        console.log('Available functions in window:', Object.keys(window).filter(k => k.includes('Response') || k.includes('Customer')));
    }
    
    // Also store in notifications array
    notifications.push(paymentResponse);
    if (typeof window.refreshMerchantBills === "function") {
        setTimeout(() => window.refreshMerchantBills(), 1500);
    }
    //console.log('🏪 ========================================================');
}

savePaymentDatabyMerchant = async (paymentData , billId) => {
    try {
        const resp = await fetch(`/merchant/save/payment/data/${billId}`, {    
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(paymentData)
        });
        if (!resp.ok) {
            throw new Error(`Failed to save payment data with status ${resp.status}`);
        }   
        const data = await resp.json();
        //console.log('Payment data saved successfully:', data);
    }   
    catch (err) {
        console.error('Error saving payment data:', err);
    }   
}

updateBillByMerchant = async (billId , status) => {
    try {
        const resp = await fetch(`/merchant/update/bill/status/${billId}/${status}`, {
            method: 'PUT', 
        });
        if (!resp.ok) {
            throw new Error(`Failed to update bill status with status ${resp.status}`);
        }
        const data = await resp.json();
       // console.log('Bill status updated successfully:', data);
    }
    catch (err) {
        console.error('Error updating bill status:', err.error);
    }               
}   


function renderNotifications() {
    // This function is now deprecated, use renderCustomerNotifications
    const notificationsList = document.getElementById('notificationsList');
    
    if (!notificationsList) return;
    
    if (notifications.length === 0) {
        notificationsList.innerHTML = '<div class="text-muted text-center py-3">No notifications yet</div>';
        return;
    }
    
    notificationsList.innerHTML = notifications.map((notif, index) => `
        <div class="card mb-2" id="notification-${index}">
            <div class="card-body">
                <div class="fw-semibold">💸 Bill from ${notif.merchantName}</div>
                <div class="text-muted">Bill ID: ${notif.billId}</div>
                <div class="mt-2">Amount: ₹${parseFloat(notif.amount).toFixed(2)}</div>
                <div>Title: ${notif.title}</div>
                <div class="mt-3 d-flex gap-2">
                    <button class="btn btn-success btn-sm" onclick="handlePay(${index}, ${parseFloat(notif.amount).toFixed(2)})">
                        Pay ₹${parseFloat(notif.amount).toFixed(2)}
                    </button>
                    <button class="btn btn-outline-danger btn-sm" onclick="handleDecline(${index})">
                        Decline
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

function handlePay(index, amount) {
    const response = confirm(`Pay received bill amount? ₹${amount.toFixed(2)}`);
    if (response) {
        alert(`Processing payment of ₹${amount.toFixed(2)}...`);
        // Remove the notification after paying
        notifications.splice(index, 1);
        renderNotifications();
    }
}

function handleDecline(index) {
    const confirmed = confirm('Are you sure you want to decline this bill?');
    if (confirmed) {
        notifications.splice(index, 1);
        renderNotifications();
        alert('Bill declined!');
    }
}

function sendBill(bill,sendBtn) {
    try {
            // Send via WebSocket (assuming stomp connection is available)
            if (typeof stompClient !== "undefined" && stompClient?.connected) {
                stompClient.send("/app/bill.send", {}, JSON.stringify(bill));
            }

            // Update button state
            setTimeout(() => {
                sendBtn.innerText = "✓ Sent";
                sendBtn.style.background = "#9e9e9e";
                sendBtn.style.cursor = "not-allowed";
            }, 500);

        } catch (err) {
            console.error("Error sending bill:", err);
            sendBtn.disabled = false;
            sendBtn.innerText = "Send Bill";
        }
}

// Make functions globally available
window.handleCustomerPay = handleCustomerPay;
window.handleCustomerDecline = handleCustomerDecline;
window.handlePay = handlePay;
window.handleDecline = handleDecline;
window.sendBill = sendBill;
window.createConnection = createConnection;
