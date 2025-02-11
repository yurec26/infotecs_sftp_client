package org.example.constants;

import java.util.regex.Pattern;

public final class MessagesConstants {
    public static final String WELCOME_MSG = "Введите адрес, порт, логин и пароль SFTP-сервера " +
            "в одну строчку через пробел, \n";
    public static final String WELCOME_MSG_EXIT = "Для завершения просто нажмите enter: ";
    public static final String MENU_MSG = "Введите номер операции из списка: ";
    public static final String GET_DOMAINS_MSG = "1. Получение списка пар \"домен – адрес\". ";
    public static final String GET_IP_BY_NAME_MSG = "2. Получение IP-адреса по доменному имени.";
    public static final String GET_NAME_BY_IP_MSG = "3. Получение доменного имени по IP-адресу.";
    public static final String ADD_DOMAIN_MSG = "4. Добавление новой пары \"домен – адрес\" в файл.";
    public static final String DELETE_DOMAIN_MSG = "5. Удаление пары \"домен – адрес\" по доменному имени.";
    public static final String EXIT_MSG = "6. Разорвать соединение с сервером.";
    public static final String UNDEFINED_COMMAND_MSG = "Такой команды нет, попробуйте ещё раз.";
    public static final String ILLEGAL_INPUT_MSG = "Неправильный ввод, повторите попытку.";
    public static final String SERVER_CONNECTION_ERROR_MSG = "Неудачная попытка подключения " +
            "к серверу, проверьте вводимые хост, порт, имя/пароль пользователя: ";

    public static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    public static String getMenu() {
        return MessagesConstants.MENU_MSG + "\n" +
                MessagesConstants.GET_DOMAINS_MSG + "\n" +
                MessagesConstants.GET_IP_BY_NAME_MSG + "\n" +
                MessagesConstants.GET_NAME_BY_IP_MSG + "\n" +
                MessagesConstants.ADD_DOMAIN_MSG + "\n" +
                MessagesConstants.DELETE_DOMAIN_MSG + "\n" +
                MessagesConstants.EXIT_MSG;
    }

    public static void print(String message) {
        System.out.println(message);
    }
}
