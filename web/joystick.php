<?
//   header('Content-Type: text/plain;'); 
  set_time_limit(0); 
  ob_implicit_flush(); 
if(isset($_POST['selfiebotid'])){
echo $id;
  $address = '46.38.49.133'; 
  $port = 4445;

  if (($socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) < 0) {
    echo "Error sock creation\n";
  }
  else {
    echo "Socket created\n";
  }
  $result = socket_connect($socket, "46.38.49.133", 4445);

 
  if ($result === false) {
    echo "Error while connecting";
  } else {
    echo "Connection successful\n";
  }

  $idStr = $_POST['selfiebotid']."\r";
  $msg = $idStr;
  
  echo "\nMsg to server: $idStr\n";
  socket_write($socket, $idStr, strlen($idStr)); 
  $out = socket_read($socket, 1024);
  echo "\nMsg from server: <<$out>>\n";
  
  
  }elseif(isset($_POST['cmd'])){
      if(strcmp($_POST['cmd'],"left")==0){
        $cmd="aaaaa";
	echo "Turn left";
      }elseif(strcmp($_POST['cmd'],"right")==0){
      $cmd="ddddd";
      echo "Turn right";
      }elseif(strcmp($_POST['cmd'],"up")==0){
      $cmd="wwwww";
      echo "Turn up";
      }elseif(strcmp($_POST['cmd'],"down")==0){
      $cmd="sssss";
      echo "Turn down";
      }elseif(strcmp($_POST['cmd'],"stop")==0){
      echo "stop $cmd";
      }
      if(strcmp($_POST['cmd'],"close")==0){
      $cmd="qqqqq";
      socket_write($socket, $cmd, strlen($cmd)); 
            echo "Connection closed\n";
	  if (isset($socket)) {
	    socket_close($socket);
	    echo "Socket successfully closed\n";
	  }
      }else{
      socket_write($socket, $cmd, strlen($cmd)); 
      }
  }
?>