package org.example.listener;

import com.jcraft.jsch.JSchException;
import org.example.constants.MessagesConstants;
import org.example.server.ServerUtil;

import java.util.Scanner;

import static org.example.constants.MessagesConstants.print;

public class OperationListener {

    private final Scanner scanner;
    private final ServerUtil serverUtil;

    public OperationListener(ServerUtil serverUtil) {
        this.serverUtil = serverUtil;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            print(MessagesConstants.WELCOME_MSG + MessagesConstants.WELCOME_MSG_EXIT);
            String inputString = scanner.nextLine();
            if (inputString.isEmpty()) break;
            String[] inputArgs = inputString.split(" ");
            try {
                int port = throwIfIllegalInput(inputArgs);
                try {
                    serverUtil.connectToServer(
                            inputArgs[0],
                            port,
                            inputArgs[2],
                            inputArgs[3]
                    );
                } catch (JSchException e) {
                    print(MessagesConstants.SERVER_CONNECTION_ERROR_MSG + e.getMessage());
                }
            } catch (IllegalArgumentException e) {
                print(e.getMessage());
            }
        }
    }

    public int throwIfIllegalInput(String[] args) throws IllegalArgumentException {
        if (args.length != 4) {
            throw new IllegalArgumentException(MessagesConstants.ILLEGAL_INPUT_MSG);
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MessagesConstants.ILLEGAL_INPUT_MSG);
        }
    }
}
