package com.example.misoluchun

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

data class TodoItem(var text: String, var done: Boolean)

class MainActivity : AppCompatActivity() {

    private lateinit var inputEdit: EditText
    private lateinit var addButton: Button
    private lateinit var listView: ListView
    private val prefsKey = "one_screen_todos"

    private val todos = mutableListOf<TodoItem>()
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputEdit = findViewById(R.id.inputEdit)
        addButton = findViewById(R.id.addButton)
        listView = findViewById(R.id.listView)

        adapter = TodoAdapter(this, todos)
        listView.adapter = adapter

        loadTodos()

        addButton.setOnClickListener {
            val text = inputEdit.text.toString().trim()
            if (text.isNotEmpty()) {
                todos.add(0, TodoItem(text, false)) // yangi element tepadan
                adapter.notifyDataSetChanged()
                inputEdit.text.clear()
                saveTodos()
            } else {
                Toast.makeText(this, "Iltimos, vazifa kiriting", Toast.LENGTH_SHORT).show()
            }
        }

        // Enter tugmasi bilan ham qo'shish
        inputEdit.setOnEditorActionListener { _, _, _ ->
            addButton.performClick()
            true
        }

        // Qisqacha ko'rsatish: bosish -> done toggle; uzoq bosish -> o'chirish
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = todos[position]
            item.done = !item.done
            adapter.notifyDataSetChanged()
            saveTodos()
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val item = todos[position]
            val dialog = android.app.AlertDialog.Builder(this)
                .setTitle("O'chirish")
                .setMessage("“${item.text}” ni o'chirishni xohlaysizmi?")
                .setPositiveButton("Ha") { _, _ ->
                    todos.removeAt(position)
                    adapter.notifyDataSetChanged()
                    saveTodos()
                }
                .setNegativeButton("Bekor", null)
                .create()
            dialog.show()
            true
        }
    }

    private fun saveTodos() {
        val arr = JSONArray()
        for (t in todos) {
            val o = JSONObject()
            o.put("text", t.text)
            o.put("done", t.done)
            arr.put(o)
        }
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(prefsKey, arr.toString()).apply()
    }

    private fun loadTodos() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString(prefsKey, "[]") ?: "[]"
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val text = o.optString("text", "")
                val done = o.optBoolean("done", false)
                todos.add(TodoItem(text, done))
            }
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Oddiy custom adapter: har bir Item uchun TextView
    class TodoAdapter(private val ctx: Context, private val items: List<TodoItem>) : BaseAdapter() {
        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Any = items[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(ctx).inflate(android.R.layout.simple_list_item_1, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            val item = items[position]
            val display = item.text
            if (item.done) {
                val s = SpannableString(display)
                s.setSpan(StrikethroughSpan(), 0, display.length, 0)
                textView.text = s
                textView.alpha = 0.6f
            } else {
                textView.text = display
                textView.alpha = 1.0f
            }
            textView.textSize = 16f
            return view
        }
    }
}
