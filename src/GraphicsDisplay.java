import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {
    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean rotate = false;
    private boolean showMarkers = true;
    private boolean showGrid = false;
    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;

    public GraphicsDisplay() {
// Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
// Сконструировать необходимые объекты, используемые в рисовании
// Перо для рисования графика
        float[] dash = {21,10,3,10,12,10,3,10,21,10};
        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 22.0f, dash, 0.0f);
// Перо для рисования осей координат
        axisStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
// Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
// Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
// Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }

    // Методы-модификаторы для изменения параметров отображения графика
// Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }
    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g) {
        /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
         * Эта функциональность - единственное, что осталось в наследство от
         * paintComponent класса JPanel
         */
        super.paintComponent(g);
// Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
        if (graphicsData == null || graphicsData.length == 0) return;
// Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
// Это необходимо для определения области пространства, подлежащей отображению
// Еѐ верхний левый угол это (minX, maxY) - правый нижний это (maxX, minY)
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
// Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
/* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X
и Y - сколько пикселов
* приходится на единицу длины по X и по Y
*/
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
// Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен  быть одинаков
// Выбираем за основу минимальный
        scale = Math.min(scaleX, scaleY);
// Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
        if (scale == scaleX) {
/* Если за основу был взят масштаб по оси X, значит по оси Y
делений меньше,
* т.е. подлежащий визуализации диапазон по Y будет меньше
высоты окна.
* Значит необходимо добавить делений, сделаем это так:
* 1) Вычислим, сколько делений влезет по Y при выбранном
масштабе - getSize().getHeight()/scale
* 2) Вычтем из этого сколько делений требовалось изначально
* 3) Набросим по половине недостающего расстояния на maxY и
minY
*/
            double yIncrement = (getSize().getHeight() / scale - (maxY -
                    minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
// Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement = (getSize().getWidth() / scale - (maxX -
                    minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
// Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
// Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
// Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
// Первыми (если нужно) отрисовываются оси координат.

        if (rotate) {
            // Сохраняем текущее преобразование
            canvas.translate(getSize().getWidth()/2, getSize().getHeight()/2);
            // Поворачиваем на 90 градусов против часовой стрелки
            canvas.rotate(-Math.PI/2);
            // Масштабируем, чтобы график занимал всё окно
            if (getSize().getWidth() > getSize().getHeight()) {
                canvas.scale(getSize().getHeight()/getSize().getWidth(),
                        getSize().getWidth()/getSize().getHeight());
            } else {
                canvas.scale(getSize().getHeight()/getSize().getWidth(),
                        getSize().getWidth()/getSize().getHeight());
            }
            // Возвращаем в центр
            canvas.translate(-getSize().getWidth()/2, -getSize().getHeight()/2);
        }

        if (showAxis) paintAxis(canvas);

        if(showGrid) paintGrid(canvas);

// Затем отображается сам график
        paintGraphics(canvas);
// Затем (если нужно) отображаются маркеры точек, по которым строился график.
        if (showMarkers) paintMarkers(canvas);
// Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    private void paintGrid(Graphics2D canvas) {
        // Шаги сетки для осей X и Y
        double gridStepX = calculateGridStep(maxX - minX);
        double gridStepY = calculateGridStep(maxY - minY);

        // Начальные координаты сетки
        double startX = Math.floor(minX / gridStepX) * gridStepX;
        double startY = Math.floor(minY / gridStepY) * gridStepY;

        // Сохраняем старые настройки
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Font oldFont = canvas.getFont();

        // Настройки для сетки
        canvas.setStroke(new BasicStroke(1.0f));
        canvas.setColor(Color.GRAY);
        canvas.setFont(new Font("Dialog", Font.PLAIN, 10));

        // Рисуем вертикальные линии сетки
        double x = startX;
        while (x <= maxX) {
            Point2D.Double point1 = xyToPoint(x, minY);
            Point2D.Double point2 = xyToPoint(x, maxY);

            // Основная линия сетки
            canvas.drawLine((int)point1.x, (int)point1.y, (int)point2.x, (int)point2.y);

            // Рисуем деления внутри ячейки
            if (x + gridStepX <= maxX) {
                double subStep = gridStepX / 10;
                for (int i = 1; i < 10; i++) {
                    double subX = x + i * subStep;
                    Point2D.Double subPoint1 = xyToPoint(subX, minY);
                    Point2D.Double subPoint2 = xyToPoint(subX, maxY);

                    // Для пятого деления (середина) делаем линию длиннее
                    if (i == 5) {
                        canvas.setStroke(new BasicStroke(0.5f));
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2.x, (int)subPoint2.y);
                    } else {
                        // Короткие штрихи для остальных делений
                        canvas.setStroke(new BasicStroke(0.3f));
                        double shortLineLength = (maxY - minY) / 50;
                        Point2D.Double subPoint2Short = xyToPoint(subX, minY + shortLineLength);
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2Short.x, (int)subPoint2Short.y);
                    }
                }
            }

            // Подписи координат
            canvas.drawString(String.format("%.2f", x), (int)point1.x - 20, getHeight() - 5);
            x += gridStepX;
        }

        // Рисуем горизонтальные линии сетки
        double y = startY;
        while (y <= maxY) {
            Point2D.Double point1 = xyToPoint(minX, y);
            Point2D.Double point2 = xyToPoint(maxX, y);

            // Основная линия сетки
            canvas.drawLine((int)point1.x, (int)point1.y, (int)point2.x, (int)point2.y);

            // Рисуем деления внутри ячейки
            if (y + gridStepY <= maxY) {
                double subStep = gridStepY / 10;
                for (int i = 1; i < 10; i++) {
                    double subY = y + i * subStep;
                    Point2D.Double subPoint1 = xyToPoint(minX, subY);
                    Point2D.Double subPoint2 = xyToPoint(maxX, subY);

                    // Для пятого деления (середина) делаем линию длиннее
                    if (i == 5) {
                        canvas.setStroke(new BasicStroke(0.5f));
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2.x, (int)subPoint2.y);
                    } else {
                        // Короткие штрихи для остальных делений
                        canvas.setStroke(new BasicStroke(0.3f));
                        double shortLineLength = (maxX - minX) / 50;
                        Point2D.Double subPoint2Short = xyToPoint(minX + shortLineLength, subY);
                        canvas.drawLine((int)subPoint1.x, (int)subPoint1.y,
                                (int)subPoint2Short.x, (int)subPoint2Short.y);
                    }
                }
            }

            // Подписи координат
            canvas.drawString(String.format("%.2f", y), 5, (int)point1.y + 5);
            y += gridStepY;
        }

        // Восстанавливаем настройки
        canvas.setStroke(oldStroke);
        canvas.setColor(oldColor);
        canvas.setFont(oldFont);
    }

    // Вспомогательная функция для расчета оптимального шага сетки
    private double calculateGridStep(double range) {
        // Желаемое количество делений (от 5 до 20)
        int desiredDivisions = 10;

        // Находим приближенный шаг
        double roughStep = range / desiredDivisions;

        // Получаем порядок величины шага
        double power = Math.floor(Math.log10(roughStep));
        double magnitude = Math.pow(10, power);

        // Нормализованный шаг (от 0.1 до 1.0)
        double normalizedStep = roughStep / magnitude;

        // Выбираем ближайший "красивый" шаг
        double[] steps = {0.1, 0.2, 0.5, 1.0, 20.0,50.0,100.0};
        double bestStep = steps[0];
        double minDiff = Math.abs(normalizedStep - steps[0]);

        for (double step : steps) {
            double diff = Math.abs(normalizedStep - step);
            if (diff < minDiff) {
                minDiff = diff;
                bestStep = step;
            }
        }

        return bestStep * magnitude;
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
// Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
// Выбрать цвет линии
        canvas.setColor(Color.RED);
/* Будем рисовать линию графика как путь, состоящий из множества
сегментов (GeneralPath)
* Начало пути устанавливается в первую точку графика, после чего
прямой соединяется со
* следующими точками
*/
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
// Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0],
                    graphicsData[i][1]);
            if (i > 0) {
// Не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
// Первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
// Отобразить график
        canvas.draw(graphics);
    }



    protected void paintMarkers(Graphics2D canvas) {
        // Установить специальное перо для черчения контуров маркеров
        canvas.setStroke(markerStroke);
        // Выбрать красный цвет для контуров маркеров

        //canvas.setColor(Color.GREEN);

        // Организовать цикл по всем точкам графика
        for (Double[] point : graphicsData) {
            // Получить координаты точки
            Point2D.Double center = xyToPoint(point[0], point[1]);

            if (hasOnlyEvenDigits(point[1])) {
                canvas.setColor(Color.BLUE);
            } else {
                canvas.setColor(Color.BLACK); // Иначе красным
            }
            // Размер основных линий креста (половина полного размера)
            int mainSize = 5; // полный размер будет 11 точек
            // Размер перпендикулярных линий на концах (полная длина)
            int crossSize = 4; // по 2 точки в каждую сторону

            // Рисуем горизонтальную линию креста
            canvas.draw(new Line2D.Double(
                    center.x - mainSize, center.y,
                    center.x + mainSize, center.y));

            // Рисуем вертикальную линию креста
            canvas.draw(new Line2D.Double(
                    center.x, center.y - mainSize,
                    center.x, center.y + mainSize));

            // Рисуем перпендикулярные линии на концах
            // Верхний конец
            canvas.draw(new Line2D.Double(
                    center.x - crossSize/2, center.y + mainSize,
                    center.x + crossSize/2, center.y + mainSize
            ));

            // Нижний конец
            canvas.draw(new Line2D.Double(
                    center.x - crossSize/2, center.y - mainSize,
                    center.x + crossSize/2, center.y - mainSize
            ));

            // Левый конец
            canvas.draw(new Line2D.Double(
                    center.x - mainSize, center.y - crossSize/2,
                    center.x - mainSize, center.y + crossSize/2
            ));

            // Правый конец
            canvas.draw(new Line2D.Double(
                    center.x + mainSize, center.y - crossSize/2,
                    center.x + mainSize, center.y + crossSize/2
            ));

        }
    }

    protected boolean hasOnlyEvenDigits(double value) {
        // Получаем целую часть числа
        int integerPart = (int) Math.abs(value);

        // Преобразуем число в строку для анализа цифр
        String numStr = String.valueOf(integerPart);

        // Проверяем каждую цифру
        for (int i = 0; i < numStr.length(); i++) {
            int digit = Character.getNumericValue(numStr.charAt(i));
            if (digit % 2 != 0) { // Если цифра нечётная
                return false;
            }
        }
        return true;
    }


    protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей

        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемой области (minX) <= 0.0,
            // а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
// Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительными координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5,
                    arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительными координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10,
                    arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
// Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемой области (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
// Стрелка оси X
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительными координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                    arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x", (float) (labelPos.getX() -
                    bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    /* Метод-помощник, осуществляющий преобразование координат.
    * Оно необходимо, т.к. верхнему левому углу холста с координатами
    * (0.0, 0.0) соответствует точка графика с координатами (minX, maxY),
    где
    * minX - это самое "левое" значение X, а
    * maxY - самое "верхнее" значение Y.
    */
    protected Point2D.Double xyToPoint(double x, double y) {
// Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - minX;
// Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

}