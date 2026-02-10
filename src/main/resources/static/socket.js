
var stompClient = null;

// Store notifications
var notifications = [];

function createConnection(role) {
    
    if(stompClient!=null && stompClient.connected){
        console.log('already connected')
        return ; 
    }
    var socket  =  new SockJS("http://localhost:8000/billbuddy") ; 
    stompClient = Stomp.over(socket) ; 
    
    
    // send Authorization : <Bearer Token> in header while jwt
    stompClient.connect( {
        'role' : role 
    } , connectCallBack ) ; 

    function connectCallBack(frame){
        console.log("after connection request frame " , frame) ; 

        stompClient.subscribe('/user/queue/notify' , subscribeCallBack) ; 

    }

    function subscribeCallBack(message){
         
        if(message.body){
            console.log('received notification : ' , message.body) ; 
            
            // Parse the message
            try {
                const notification = JSON.parse(message.body);
                console.log('Parsed notification:', notification);
                
                // Add to notifications array
                addNotification(notification);
                
            } catch (err) {
                console.error('Failed to parse notification:', err);
            }
        }
    }

}

function addNotification(notification) {
    // Add notification to array
    notifications.push(notification);
    
    // Render notifications
    renderNotifications();
}

function renderNotifications() {
    const notificationsList = document.getElementById('notificationsList');
    
    if (!notificationsList) return;
    
    if (notifications.length === 0) {
        notificationsList.innerHTML = '<div class="empty-notifications">No notifications yet</div>';
        return;
    }
    
    notificationsList.innerHTML = notifications.map((notif, index) => `
        <div class="notification-item" id="notification-${index}">
            <div class="notification-header">
                💸 Bill from ${notif.merchantName}
            </div>
            <div class="notification-details">
                <p><strong>Amount:</strong> ₹${parseFloat(notif.amount).toFixed(2)}</p>
                <p><strong>Title:</strong> ${notif.title}</p>
                <p><strong>Bill ID:</strong> ${notif.billId}</p>
            </div>
            <div class="notification-actions">
                <button class="pay-btn" onclick="handlePay(${index}, ${parseFloat(notif.amount).toFixed(2)})">
                    Pay ₹${parseFloat(notif.amount).toFixed(2)}
                </button>
                <button class="decline-btn" onclick="handleDecline(${index})">
                    Decline
                </button>
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

function sendBill() {
    var res = stompClient.send('/app/bill.send' , {} , JSON.stringify(sendBillPayload)) ; 
    console.log('send bill => => => ' , res) ; 
}
