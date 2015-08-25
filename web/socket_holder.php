<?php
echo "start script\n";
if(isset($_POST['selfiebotid'])){
    $idStr = $_POST['selfiebotid']."\r";
    set_time_limit(0); 
    ob_implicit_flush(); 
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

    

  
    echo "\nMsg to server: $idStr\n";
    socket_write($socket, $idStr, strlen($idStr)); 
    $out = socket_read($socket, 1024);
    echo "\nMsg from server: <<$out>>\n";
    
    $msg='';
    $cnt=0;
    $key_t = msg_get_queue(ftok("msg_queue_$idStr.stat", 'R'),0666 | IPC_CREAT);
    while((strcmp($msg,"qqqqq")==0)===false){
      if (msg_receive($key_t, 1, $msg_type, 15, $msg, true, 0, $msg_error)) {
// 	 echo "got msg$cnt:$msg";
         socket_write($socket, $msg, strlen($msg)); 
      } else {
// 	echo ("Received $msg_error fetching message\n");
      }
      $cnt=$cnt+1;
    }
    if (isset($socket)) {
      socket_close($socket);
      msg_remove_queue($key_t);
    }
}
    echo "End script";
?>