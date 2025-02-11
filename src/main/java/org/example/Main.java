package org.example;

import org.example.listener.OperationListener;
import org.example.server.ServerUtil;
import org.example.service.OperationService;

public class Main {
    public static void main(String[] args) {
        OperationService operationService = new OperationService();
        ServerUtil serverUtil = new ServerUtil(operationService);
        OperationListener operationListener = new OperationListener(serverUtil);
        operationListener.run();
    }
}