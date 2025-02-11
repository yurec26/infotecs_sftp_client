package org.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.example.constants.MessagesConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.example.constants.MessagesConstants.IPV4_PATTERN;
import static org.example.constants.MessagesConstants.print;

public class OperationService {

    public void getAll(ChannelSftp channelSftp, String filePath) {
        try {
            Map<String, String> fileContentMap = readMapOfIpAddresses(channelSftp, filePath);
            print("Список хранящихся на сервере доменов: ");
            fileContentMap.entrySet().stream()
                    .map(s -> s.getKey() + " : " + s.getValue())
                    .sorted().forEach(MessagesConstants::print);

        } catch (SftpException | UnsupportedEncodingException e) {
            print("Ошибка при попытке чтения файла: " + e.getMessage());
        }
    }

    public void getIpByName(ChannelSftp channelSftp, Scanner scanner, String filePath) {
        try {
            Map<String, String> fileContentMap = readMapOfIpAddresses(channelSftp, filePath);
            print("Введите доменное имя: ");
            String input = scanner.nextLine();

            print("Результат поиска адреса по имени: ");
            String result = fileContentMap.entrySet()
                    .stream()
                    .filter(s -> s.getKey().equals(input))
                    .findFirst().orElseThrow(NoSuchElementException::new).getValue();
            print(result);
        } catch (SftpException | UnsupportedEncodingException e) {
            print("Ошибка при попытке чтения : " + e.getMessage());
        } catch (NoSuchElementException e) {
            print("По вашему запросу ничего не найдено.");
        }
    }

    public void getNameByIp(ChannelSftp channelSftp, Scanner scanner, String filePath) {
        try {
            Map<String, String> fileContentMap = readMapOfIpAddresses(channelSftp, filePath);
            print("Введите адрес : ");
            String input = scanner.nextLine();

            print("Результат поиска имени по адресу: ");
            String result = fileContentMap.entrySet()
                    .stream()
                    .filter(s -> s.getValue().equals(input))
                    .findFirst().orElseThrow(NoSuchElementException::new).getKey();
            print(result);
        } catch (SftpException | UnsupportedEncodingException e) {
            print("Ошибка при попытке чтения JSON-файла: " + e.getMessage());
        } catch (NoSuchElementException e) {
            print("По вашему запросу ничего не найдено.");
        }
    }

    public void addDomain(ChannelSftp channelSftp, Scanner scanner, String filePath) {
        try {
            Map<String, String> fileContentMap = readMapOfIpAddresses(channelSftp, filePath);

            print("Введите пару доменное имя - адрес через пробел: ");
            String[] input = scanner.nextLine().split(" ");

            if (isIpUniq(input[1], fileContentMap)) {
                if (isValidIPv4(input[1])) {
                    fileContentMap.put(input[0], input[1]);
                    writeMapAsJson(channelSftp, fileContentMap, filePath);
                } else {
                    print("Невалидный IP-адрес.");
                }
            } else {
                print("Такой IP-адрес уже есть в базе.");
            }
        } catch (SftpException | UnsupportedEncodingException e) {
            print("Ошибка при попытке чтения файла: " + e.getMessage());
        }
    }

    public void deleteDomain(ChannelSftp channelSftp, Scanner scanner, String filePath) {
        try {
            Map<String, String> fileContentMap = readMapOfIpAddresses(channelSftp, filePath);
            print("Введите доменное имя для удаления: ");
            String input = scanner.nextLine();
            String key = fileContentMap.entrySet().stream()
                    .filter(s -> s.getKey().equals(input))
                    .findFirst().orElseThrow(NoSuchElementException::new)
                    .getKey();
            fileContentMap.remove(key);
            writeMapAsJson(channelSftp, fileContentMap, filePath);
        } catch (SftpException | UnsupportedEncodingException e) {
            print("Ошибка при попытке чтения файла: " + e.getMessage());
        } catch (NoSuchElementException e) {
            print("По вашему запросу ничего не найдено.");
        }
    }

    public Map<String, String> readMapOfIpAddresses(ChannelSftp channelSftp, String remoteFile)
            throws SftpException, UnsupportedEncodingException {
        try {
            channelSftp.ls(remoteFile);
        } catch (SftpException e) {
            print("Ошибка: файл не найден на сервере. Причина: " + e.getMessage());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        channelSftp.get(remoteFile, outputStream);
        String originalContent = outputStream.toString(StandardCharsets.UTF_8.name());
        if (originalContent == null || originalContent.trim().isEmpty()) {
            originalContent = "{}";
        }

        JsonObject jsonObject = new Gson().fromJson(originalContent, JsonObject.class);

        JsonArray addressesArray = jsonObject.has("addresses")
                && !jsonObject.get("addresses").isJsonNull()
                ? jsonObject.getAsJsonArray("addresses")
                : new JsonArray();

        Map<String, String> domainIpMap = new HashMap<>();
        for (int i = 0; i < addressesArray.size(); i++) {
            JsonObject addressObject = addressesArray.get(i).getAsJsonObject();
            String domain = addressObject.get("domain").getAsString();
            String ip = addressObject.get("ip").getAsString();
            domainIpMap.put(domain, ip);
        }
        return domainIpMap;
    }

    public void writeMapAsJson(ChannelSftp channelSftp, Map<String, String> map, String remoteFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootObject = new JsonObject();
        JsonArray addressesArray = new JsonArray();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            JsonObject addressObject = new JsonObject();
            addressObject.addProperty("domain", entry.getKey());
            addressObject.addProperty("ip", entry.getValue());
            addressesArray.add(addressObject);
        }
        rootObject.add("addresses", addressesArray);

        String jsonContent = gson.toJson(rootObject);
        byte[] bytes = jsonContent.getBytes(StandardCharsets.UTF_8);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            channelSftp.put(inputStream, remoteFile);
            print("Файл успешно записан.");
        } catch (IOException | SftpException e) {
            print("Ошибка записи файла: " + e.getMessage());
        }
    }

    public boolean isValidIPv4(String ip) {
        return IPV4_PATTERN.matcher(ip).matches();
    }

    public boolean isIpUniq(String ip, Map<String, String> map) {
        return map.values().stream().noneMatch(s -> s.equals(ip));
    }
}


