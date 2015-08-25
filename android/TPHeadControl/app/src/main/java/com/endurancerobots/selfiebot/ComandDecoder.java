package com.endurancerobots.selfiebot;

/**
 * Created by ilya on 03.08.15.
 */
public class ComandDecoder {
    static public String decode(byte[] cmd) {
        switch (cmd[TcpDataTransferThread.ECHO_TAG.length()]){
            case 119:
                return ("Command: UP (" + cmd[0] + ")");
            case 97:
                return("Command: LEFT (" + cmd[0] + ")");
            case 115:
                return("Command: DOWN (" + cmd[0] + ")");
            case 100:
                return("Command: RIGHT (" + cmd[0] + ")");
            case 113:
                return("Command: CLOSE CONNECTION (" + cmd[0] + ")");
            default:
                return("Unknown command: (" + cmd[0] + ")");
        }
    }
}
