<?
  header('Content-Type: text/plain;'); 
  set_time_limit(0); 
  ob_implicit_flush(); 
  $address = '46.38.49.133'; 
  $port = 4445;

  if (($socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) < 0) {
    echo "Error sock creation";
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

  $idStr = "G987654321\r";
  $msg = $idStr;
  
  echo "Msg to server: $idStr\n";
  socket_write($socket, $idStr, strlen($idStr)); 


      $out = socket_read($socket, 1024);
      echo "Msg from server: $out.\n";
  $msg = "aaaaa";
  socket_write($socket, $msg, strlen($msg));

//   }

//   echo "Connection closed\n";
//   if (isset($socket)) {
//     socket_close($socket);
//     echo "Socket successfully closed";
//   }
?>