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

var strId = '987654321';
function connect(){
  if(connected===false){
    strId = document.getElementById("login").value;
    
    connectReq.open("POST", 'socket_holder.php', true);
    connectReq.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    connectReq.send('selfiebotid=G'+strId+'&ajax=1');
    
    document.getElementById("connect").innerHTML ="Disconnect";
    connected=true;
    
    document.getElementById("up").onclick=function(){sendCmd("wwwww");};
    document.getElementById("down").onclick=function(){sendCmd("sssss");};
    document.getElementById("left").onclick=function(){sendCmd("aaaaa");};
    document.getElementById("right").onclick=function(){sendCmd("ddddd");};
   }else{
    closeSocket();
    document.getElementById("connect").innerHTML ="Connect";
    connected=false;
   }
}
function sendCmd(cmd){
//   if(connected){
    req.open("POST", 'joystick.php', true);
    req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    req.send('cmd='+cmd+'&selfiebotid=G'+strId+'&ajax=1');
//   }else{
//     alert("Connect to SelfieBot, please.");
//   }
}
function closeSocket(){
  if(confirm("Do you want to disconnect from Selfiebot?")){
    sendCmd("qqqqq");
  }
}
