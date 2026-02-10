
var stompClient = null ; 

function createConnection(role) {
    var socket  =  new SockJS("http://localhost:8000/billbuddy") ; 
    stompClient = Stomp.over(socket) ; 
    
    // send Authorization : <Bearer Token> in header while jwt
    stompClient.connect( {} , connectCallBack ) ; 

    function connectCallBack(frame){
        console.log("after connection request frame " , frame) ; 
    }

}