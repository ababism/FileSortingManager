package io.github.ababism.dependencySortApp.FileSorter;

import io.github.ababism.dependencySortApp.Graph.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Класс реализующий обработку файлов и ее сортировку соотвественно с тз
 */
public class FileSorter {
    /**
     * Поля
     * chefDirectory - содержит корневую папку
     * files - содержит списков файлов в корневой папке
     * path - содержит относительные пути до файлов папки
     * graph - содержит отображения зависимостей в файлах
     */
    private List<File> files = new ArrayList<>();
    private List<String> paths = new ArrayList<>();
    private Graph graph;
    private final File chefDirectory;

    /**
     * Конструктор он корневой папки, выводит сообщения в консоль о коррекности создания
     *
     * @param rootDirectoryName абослюный путь до корневой папки
     */
    public FileSorter(String rootDirectoryName) {
        chefDirectory = new File(rootDirectoryName);
        initializeFiles();
    }

    /**
     * Инциализирует или обновляет файлы в папке и выводит сообщения в консоль
     *
     * @return правильно ли указана корневая папка
     */
    private boolean initializeFiles() {
        files = new ArrayList<>();

        addFilesFrom(chefDirectory);
        if (isRootLegit()) {
            System.out.println("Файлы из корневой дирертории" + chefDirectory.getAbsolutePath() + " инициализированны");
            return true;
        } else {
            System.out.println("Путь до корневой директории указан некорректно");
            return false;
        }
    }

    /**
     * Проверка корневой папки
     *
     * @return правильно ли указана корневая папка
     */
    private boolean isRootLegit() {
        return chefDirectory.exists();
    }

    /**
     * Произодится конкатинация файлов
     */
    public void concatenateFiles() {
        System.out.println("--------------------------------------------------");
        System.out.println("Конкатенация файлов отсортированных по зависимостям");
        if (!initializeFiles()) {
            System.out.println("Не удалось инициализировать файлы, конкатенация невозможна");
            System.out.println("--------------------------------------------------");
            return;
        }
        if (parseFilesToGraph()) {
            System.out.println("Файлы успешно обаботаны");
            dependencySort();
        } else {
            System.out.println("Не удалось обработать файлы, исправте ошибку и попробуйте еще раз");
            return;
        }
        System.out.println("--------------------------------------------------");
    }

    /**
     * Сортировка сделанная согласно тз
     * Выводит информацию в консоль
     */
    private void dependencySort() {
        if (!isAcyclic()) {
            System.out.println("Указанная папка содержит циклические зависимоти: " + chefDirectory.getAbsolutePath());
            System.out.println("Отсортировать невозможно, удалите циклические зависимости");
            return;
        }
        List<Integer> sortedIndexes = graph.topologicalSort().stream().toList();
        System.out.println("Файлы успешно отсорированны в следующем порядке");
        for (var index : sortedIndexes) {
            System.out.println(paths.get(index));
        }

        System.out.println("Сам результат конкатенации");
        StringBuilder stringBuilder = new StringBuilder();
        for (var index : sortedIndexes) {
            try {
                stringBuilder.append(Files.readString(files.get(index).toPath()));
                stringBuilder.append('\n');
            } catch (IOException ioException) {
                System.out.println("IO Exception: " + ioException.getMessage());
            } catch (SecurityException securityException) {
                System.out.println("Проблема доступа к файлам убедитесь," +
                        " что программе дано разрешение на работу с файлами: " + securityException.getMessage());
            }
        }
        System.out.println(stringBuilder);
    }


    /**
     * Для нициализации и обновляния поля files
     *
     * @param fileOrDirectory папка из которой надо добавить файлы
     */
    private void addFilesFrom(File fileOrDirectory) {
        try {
            // проверка на null чтобы избежать ошибки
            if (fileOrDirectory == null || !fileOrDirectory.exists()) {
                return;
            }
            if (fileOrDirectory.isFile()) {
                if (!fileOrDirectory.canRead()) {
                    System.out.println("Cannot read file, this file will be ignored: "
                            + fileOrDirectory.getAbsolutePath());
                }
                files.add(fileOrDirectory);
                return;
            }
        } catch (SecurityException securityException) {
            System.out.println("Произошла ошибка доступа, это файл или директория будут проигнорированы"
                    + securityException.getMessage());
        }

        if (fileOrDirectory.isDirectory()) {
            // еще проверка на null чтобы избежать ошибки
            if (fileOrDirectory.listFiles() == null) {
                return;
            }
            for (var file : fileOrDirectory.listFiles()) {
                addFilesFrom(file);
            }
        }
    }


    /**
     * Отображает файлы с завсисимостями в соотвествующий им граф, для последующей работы
     *
     * @return получилось ли прочитать зависимости с файлов, нет ли неправильных зависивостей
     */
    private boolean parseFilesToGraph() {
        // Записываем названия имеющихся файлов относительно папки
        paths = new ArrayList<>();
        System.out.println("Файлы для обработки:");

        for (var file : files) {
            paths.add(file.getAbsolutePath().replace(chefDirectory.getAbsolutePath()
                    + File.separator, ""));
            System.out.println(file.getAbsolutePath().replace(chefDirectory.getAbsolutePath()
                    + File.separator, ""));
        }
        Scanner scanner;
        File file;
        String currentString;
        String currentRequirement;
        int requirementIndex;

        graph = new Graph(files.size());
        for (int currentIndex = 0; currentIndex < files.size(); ++currentIndex) {
            file = files.get(currentIndex);

            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot find file: " + file.getAbsolutePath());
                return false;
            }
            while (scanner.hasNextLine()) {
                currentString = scanner.nextLine();
                if (currentString.startsWith("require '")) {
                    currentRequirement = currentString.replace("require ", "").
                            replace("'", "");
                    if (paths.contains(currentRequirement)) {
                        requirementIndex = paths.indexOf(currentRequirement);
                        graph.addEdge(requirementIndex, currentIndex);
                    } else {
                        System.out.println("required file hasn't been found: " + currentRequirement);
                        return false;
                    }
                }
            }
            scanner.close();
        }
        return true;
    }

    /**
     * Проверяет ацикличен ли наш ориентированный граф в graph
     */
    private boolean isAcyclic() {
        return graph.isAcyclic();
    }

}
