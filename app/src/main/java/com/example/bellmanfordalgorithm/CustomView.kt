// src/main/java/com/example/drawpointsandlines/CustomView.kt
package com.example.bellmanfordalgorithm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class Node(var x: Float, var y: Float, val name: Int)
data class Edge(val start: Node, val end: Node, val weight: Int)


class CustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Mode { NODE, EDGE, PATH }

    // Список вершин и рёбер
    private var nodes = mutableListOf<Node>()
    private var graph = mutableListOf<Edge>()

    // Данные
    private var mode = Mode.NODE
    private var selectedNodeIndex: Int? = null
    private var history = mutableListOf<Any>()
    private var highlightedPath = mutableListOf<Edge>()
    private var distances = mutableListOf<Int>()
    var infoStr: String = ""

    // Определение кистей для рисования
    private var paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 12f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    @SuppressLint("ResourceAsColor")
    private var highlightPaint = Paint(paint).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.btn)
    }

    @SuppressLint("ResourceAsColor")
    private var highlightCirclePaint = Paint(paint).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.btn)
    }
    private var textPaint = Paint().apply {
        textSize = 60f
        color = Color.BLACK
    }


    // Метод для рисования точек и линий
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        graph.forEach { edge ->
            drawEdge(canvas, edge, paint)
        }

        highlightedPath.forEach { edge ->
            drawEdge(canvas, edge, highlightPaint)
        }

        for (i in nodes.indices) {
            val node = nodes[i]
            val tempPaint =
                if (i == selectedNodeIndex && mode != Mode.NODE) {
                    canvas.drawCircle(node.x, node.y, 50f, highlightPaint)
                    canvas.drawCircle(node.x, node.y, 50f, highlightCirclePaint)
                }
                 else canvas.drawCircle(node.x, node.y, 50f, paint)



            canvas.drawText(node.name.toString(), node.x - 17f, node.y + 20f, textPaint)
        }

        for (i in highlightedPath.indices) {
            val node1 = highlightedPath[i].start
            val node2 = highlightedPath[i].end


            canvas.drawCircle(node1.x, node1.y, 50f, highlightPaint)
            canvas.drawCircle(node1.x, node1.y, 50f, highlightCirclePaint)
            canvas.drawText(node1.name.toString(), node1.x - 17f, node1.y + 20f, textPaint)
            canvas.drawCircle(node2.x, node2.y, 50f, highlightPaint)
            canvas.drawCircle(node2.x, node2.y, 50f, highlightCirclePaint)
            canvas.drawText(node2.name.toString(), node2.x - 17f, node2.y + 20f, textPaint)
        }
    }

    private fun drawEdge(canvas: Canvas, edge: Edge, paint: Paint) {
        val start = edge.start
        val end = edge.end

        val x = (end.x - start.x).toDouble()
        val y = (end.y - start.y).toDouble()

        val hypotenuse = Math.sqrt(x * x + y * y)

        val xPlus = (50 * x / hypotenuse).toFloat()
        val yPlus = (50 * y / hypotenuse).toFloat()

        canvas.drawLine(start.x + xPlus, start.y + yPlus, end.x - xPlus, end.y - yPlus, paint)
        canvas.drawLine(end.x - xPlus, end.y - yPlus, end.x - xPlus, end.y - yPlus, paint)
        canvas.drawLine(end.x - xPlus, end.y - yPlus, end.x - xPlus, end.y - yPlus, paint)

        val arrowLength = 40f
        val arrowAngle = Math.PI / 6 // 30 градусов в радианах

        val endX = end.x - xPlus
        val endY = end.y - yPlus

        val angle = atan2(y, x)

        val arrowX1 = endX - (arrowLength * cos(angle - arrowAngle)).toFloat()
        val arrowY1 = endY - (arrowLength * sin(angle - arrowAngle)).toFloat()

        val arrowX2 = endX - (arrowLength * cos(angle + arrowAngle)).toFloat()
        val arrowY2 = endY - (arrowLength * sin(angle + arrowAngle)).toFloat()

        canvas.drawLine(endX, endY, arrowX1, arrowY1, paint)
        canvas.drawLine(endX, endY, arrowX2, arrowY2, paint)

        // Вычисляем середину ребра
        val midX = (start.x + end.x) / 2
        val midY = (start.y + end.y) / 2

        // Смещение текста относительно перпендикулярного вектора
        val offset = 40f
        val perpX = -y
        val perpY = x

        val perpHypotenuse = Math.sqrt(perpX * perpX + perpY * perpY)
        val textX = midX + (offset * perpX / perpHypotenuse).toFloat()
        val textY = midY + (offset * perpY / perpHypotenuse).toFloat()

        // Учитываем размер текста при размещении
        val weightText = edge.weight.toString()
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(weightText, 0, weightText.length, textBounds)

        val adjustedTextX = textX - textBounds.width() / 2
        val adjustedTextY = textY + textBounds.height() / 2

        if (edge.weight != Int.MAX_VALUE) {
            canvas.drawText(weightText, adjustedTextX, adjustedTextY, textPaint)
        }
    }

    // Обработка касания
    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (mode) {

            Mode.NODE -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        for (i in nodes.indices) {
                            val node = nodes[i]
                            if (isNodeTouched(node, event)) {
                                selectedNodeIndex = i
                                return true
                            }
                        }
                        nodes.add(Node(event.x, event.y, nodes.size))
                        history.add(Node(event.x, event.y, nodes.size))
                        invalidate()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        selectedNodeIndex?.let {
                            nodes[it].x = event.x
                            nodes[it].y = event.y
                            invalidate()
                            return true
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        selectedNodeIndex = null
                    }

                }
            }

            Mode.EDGE -> {
                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        if (selectedNodeIndex == null) {
                            for (i in nodes.indices) {
                                val node = nodes[i]
                                if (isNodeTouched(node, event)) {
                                    selectedNodeIndex = i
                                    invalidate()
                                    return true
                                }
                            }
                        } else {
                            for (i in nodes.indices) {
                                val node = nodes[i]
                                if (isNodeTouched(node, event)) {
                                    if (i != selectedNodeIndex) {
                                        var isSimularEdge = false
                                        graph.forEach {
                                            if (nodes[selectedNodeIndex!!].equals(it.start) &&
                                                nodes[i].equals(it.end)
                                            ) isSimularEdge = true
                                        }
                                        if (!isSimularEdge) {
                                            showWeightDialog(nodes[selectedNodeIndex!!], nodes[i])
                                        }
                                    }
                                    selectedNodeIndex = null
                                    invalidate()
                                    return true
                                }
                            }
                        }
                    }
                }
            }

            Mode.PATH -> {
                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        if (selectedNodeIndex == null) {
                            for (i in nodes.indices) {
                                val node = nodes[i]
                                if (isNodeTouched(node, event)) {
                                    selectedNodeIndex = i
                                    highlightedPath.clear()
                                    distances.clear()
                                    infoStr = ""
                                    invalidate()
                                    return true
                                }
                            }
                        } else {
                            for (i in nodes.indices) {
                                val node = nodes[i]
                                if (isNodeTouched(node, event)) {

                                    if (i != selectedNodeIndex) {
                                        try {
                                            bellmanFord(selectedNodeIndex!!, i)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "No path found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            graph.forEach {
                                                Log.e(
                                                    "test",
                                                    "${it.start.name} ${it.end.name} ${it.weight}"
                                                )
                                            }
                                            Log.e("test", e.toString())
                                        }

                                    }

                                    selectedNodeIndex = null
                                    invalidate()
                                    return true
                                }
                            }
                        }
                    }
                }
            }

        }


        return super.onTouchEvent(event)
    }


    //--------------------------------bellman Ford Algorithm-----------------------------------------

    // Основная функция, которая находит кратчайшие расстояния от src до всех других вершин, используя алгоритм Беллмана-Форда.
    // Функция также обнаруживает цикл отрицательного веса
    fun bellmanFord(src: Int, targetNode: Int) {
        // Шаг 1: Инициализируем расстояния от src до всех других вершин как БЕСКОНЕЧНОСТЬ и массив предков
        val dist = IntArray(nodes.size) { Int.MAX_VALUE }
        dist[src] = 0

        val parent = IntArray(nodes.size) { -1 }

        // Шаг 2: Расслабляем все ребра |V| - 1 раз
        for (i in 0 until nodes.size - 1) {

            for ((start, end, weight) in graph) {
                if (dist[start.name] != Int.MAX_VALUE && dist[start.name] + weight < dist[end.name]) {
                    dist[end.name] = dist[start.name] + weight
                    parent[end.name] = start.name
                }
            }
        }

        // Шаг 3: Проверяем на наличие цикла отрицательного веса
        for ((start, end, weight) in graph) {
            if (dist[start.name] != Int.MAX_VALUE && dist[start.name] + weight < dist[end.name]) {
                Toast.makeText(
                    context,
                    "The graph contains a negative weight cycle",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // запись данных
        distances = dist.toMutableList()

        val path = ArrayList<Int>()
        var currentNode = targetNode
        while (currentNode != -1) {
            path.add(currentNode)
            currentNode = parent[currentNode]
        }
        path.reverse()
        Log.e("test", "Путь до $targetNode: ${path.joinToString(" -> ")}")

        for (i in dist.indices) {
            if (i != src && dist[i] != Int.MAX_VALUE) {
                val path = getPath(parent, i)
                infoStr += "Путь до $i: ${path.joinToString(" -> ")}\n"
            }
        }


        for (i in 0 until path.size - 1) {

            Log.e("test", "${path[i]} ${path[i + 1]}")
            highlightedPath.add(
                Edge(
                    getNodeByName(path[i])!!,
                    getNodeByName(path[i + 1])!!,
                    Int.MAX_VALUE
                )
            )
        }

        if (highlightedPath.isEmpty()) Toast.makeText(
            context,
            "No path found",
            Toast.LENGTH_SHORT
        ).show()
    }

    //-----------------------------------------------------------------------------------------------

    private fun getPath(parent: IntArray, node: Int): List<Int> {
        val path = ArrayList<Int>()
        var currentNode = node
        while (currentNode != -1) {
            path.add(currentNode)
            currentNode = parent[currentNode]
        }
        path.reverse()
        return path
    }

    private fun showWeightDialog(start: Node, end: Node) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter weight")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val weight = input.text.toString().toIntOrNull()
            if (weight != null) {
                graph.add(Edge(start, end, weight))
                history.add(Edge(start, end, weight))
                invalidate()
            } else {
                Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
            }

        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun isNodeTouched(node: Node, event: MotionEvent): Boolean {
        val dx = event.x - node.x
        val dy = event.y - node.y
        return dx * dx + dy * dy <= 10000
    }

    fun stepBack() {
        highlightedPath.clear()
        if (history.isNotEmpty()) {
            when (history[history.size - 1]) {
                is Node -> {
                    nodes.removeAt(nodes.size - 1)
                    history.removeAt(history.size - 1)
                }

                is Edge -> {
                    graph.removeAt(graph.size - 1)
                    history.removeAt(history.size - 1)
                }
            }
            invalidate()
        }
    }

    fun changeMode(mode: Mode) {
        this.mode = mode
        selectedNodeIndex = null
        highlightedPath.clear()
        distances.clear()
        infoStr = ""
        invalidate()
    }

    fun getNodeByName(name: Int): Node? {
        nodes.forEach {
            if (it.name == name) return it
        }
        return null
    }

    fun delete() {
        nodes.clear()
        graph.clear()
        selectedNodeIndex = null
        highlightedPath.clear()
        history.clear()
        distances.clear()
        infoStr = ""
        invalidate()
    }

    fun getInfo(): List<Int>? {
        if (distances.isNotEmpty()) {
            return distances
        }
        return null
    }
}


