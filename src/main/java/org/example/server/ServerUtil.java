package org.example.server;

import com.jcraft.jsch.*;
import org.example.constants.MessagesConstants;
import org.example.service.OperationService;

import java.util.Scanner;

import static org.example.constants.MessagesConstants.print;


public class ServerUtil {

    private final Scanner scanner;
    private final OperationService operationService;

    public ServerUtil(OperationService operationService) {
        this.operationService = operationService;
        this.scanner = new Scanner(System.in);
    }

    public void connectToServer
            (
                    String host,
                    int port,
                    String username,
                    String password
            ) throws JSchException {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "accept-new");
            session.connect();
            print("Подключение к SFTP-серверу успешно.");
            Channel channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            processFileOperations(channelSftp);

        } catch (JSchException e) {
            throw new JSchException(e.getMessage());
        } finally {
            if (channelSftp != null) {
                try {
                    channelSftp.exit();
                } catch (Exception e) {
                    print("Ошибка разрыва соединения");
                }
            }
            if
            (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void processFileOperations(ChannelSftp channelSftp) {
        String pathToFile = null;
        while (true) {
            if (pathToFile == null) {
                print("Введите полный путь/имя файла с адресами на сервере" +
                        " или просто нажмите enter для разрыва соединения");
                String pathToDomainFile = scanner.nextLine();
                if (pathToDomainFile.isEmpty()) break;

                try {
                    channelSftp.ls(pathToDomainFile);
                    print("Файл существует на сервере.");
                    pathToFile = pathToDomainFile;

                } catch (SftpException e) {
                    print("Файл не найден на сервере.");
                }
            } else {
                print(MessagesConstants.getMenu());
                String choice = scanner.nextLine();

                if (choice.equals("6")) break;

                switch (choice) {
                    case "1":
                        operationService.getAll(channelSftp, pathToFile);
                        break;
                    case "2":
                        operationService.getIpByName(channelSftp, scanner, pathToFile);
                        break;
                    case "3":
                        operationService.getNameByIp(channelSftp, scanner, pathToFile);
                        break;
                    case "4":
                        operationService.addDomain(channelSftp, scanner, pathToFile);
                        break;
                    case "5":
                        operationService.deleteDomain(channelSftp, scanner, pathToFile);
                        break;
                    default:
                        print(MessagesConstants.UNDEFINED_COMMAND_MSG);
                        break;
                }
            }
        }
    }
}






