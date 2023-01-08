package io.github.ababism.dependencySortApp.main;

import io.github.ababism.dependencySortApp.FileSorter.FileSorter;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 * Точка входа в программу
 */
public class Main {
    public static void main(String[] args) {
        String root = "/Users/abism/Documents/Java/FileSortingManager/testDirectory";

        System.out.print("Введите полный путь до выбранной корневой папки (начиная с" + File.separator + "):");
        try {
            Scanner scanner = new Scanner(System.in);
            root = scanner.nextLine();
        } catch (NoSuchElementException elementException) {
            System.out.println("Кажется у нас не получилось обрабатывать ваш ввод с консоли: "
                    + elementException.getMessage());
        } catch (Exception ex) {
            System.out.println("Непредвиденная ошибка с вводом из консоли" + ex.getMessage());
        }

        FileSorter sorter = new FileSorter(root);
        sorter.concatenateFiles();

    }
}
