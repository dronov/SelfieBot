<?
  header('Content-Type: text/plain;'); 
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

  $idStr = "G987654321\r";
  $msg = $idStr;
  
  echo "\nMsg to server: $idStr\n";
  socket_write($socket, $idStr, strlen($idStr)); 
  $out = socket_read($socket, 1024);
  echo "\nMsg from server: <<$out>>\n";
  $i = 0;
  while($i<5){
    echo $i;
    $i=$i+1;
  }
  echo ("Script finished");

?>