var connected=false;
var req;

if (window.XMLHttpRequest) connectReq = new XMLHttpRequest(); 
else if (window.ActiveXObject) {
    try {
	connectReq = new ActiveXObject('Msxml2.XMLHTTP');
    } catch (e){
    alert("exeption!");
    }
    try {
    connectReq = new ActiveXObject('Microsoft.XMLHTTP');
    } catch (e){
    alert("exeption!");
    }
}

if (window.XMLHttpRequest) req = new XMLHttpRequest(); 
else if (window.ActiveXObject) {
    try {
	req = new ActiveXObject('Msxml2.XMLHTTP');
    } catch (e){
    alert("exeption!");
    }
    try {
    req = new ActiveXObject('Microsoft.XMLHTTP');
    } catch (e){
    alert("exeption!");
    }
}

if (req) {
  req.onreadystatechange = function() {
    if (req.readyState == 4 && req.status == 200)  
    { 
    // 	        alert(req.responseText); 
      document.getElementById("display").innerHTML = req.responseText;
    }        
  };  
} 
else alert("Браузер не поддерживает AJAX");

function connect(){
  if(connected===false){
    var strId = document.getElementById("login").value;
    send('selfiebotid=G'+strId);
    document.getElementById("connect").innerHTML ="Disconnect";
    connected=true;
   }else{
    closeSocket();
    document.getElementById("connect").innerHTML ="Connect";
    connected=false;
   }
}
function sendCmd(cmd){
  req.open("POST", 'joystick.php', true);
  req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
// 	req.setRequestHeader("Content-Type", "text/plain");
  req.send('cmd='+cmd+'&ajax=1');
}
function send(content){
  connectReq.open("POST", 'socket_holder.php', true);
  connectReq.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
// 	req.setRequestHeader("Content-Type", "text/plain");
  connectReq.send(content+'&ajax=1');
}

function closeSocket(){
  if(confirm("Do you want to disconnect from Selfiebot?")){
    sendCmd('close');
  }
}
