<?php
  echo "Joystick ";
  $strId="G987654321";
  set_time_limit(0); 
  ob_implicit_flush(); 
  if(isset($_POST['cmd'])){
    if(isset($_POST['selfiebotid'])){
    $strId = $_POST['selfiebotid'];
    
  }
  echo "send to $strId:";
  $cmd=$_POST['cmd'];
  $key_t_out = msg_get_queue(ftok("msg_queue.stat", 'R'),0666 | IPC_CREAT);
  if (!msg_send ($key_t_out, 1, $cmd, true, true, $msg_err))
    echo "Msg not sent because $msg_err\n";
  if (!msg_send ($key_t_out, 1, $cmd, true, true, $msg_err))
    echo "Msg not sent because $msg_err\n";
  if (!msg_send ($key_t_out, 1, $cmd, true, true, $msg_err))
    echo "Msg not sent because $msg_err\n";
  else echo "$cmd";
  }
?>