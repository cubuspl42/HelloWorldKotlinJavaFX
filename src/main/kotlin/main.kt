import javafx.application.Application
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage


fun <Outer, Inner> ObservableValue<Outer>.let(function: (t: Outer) -> ObservableValue<Inner>): ObservableValue<Inner> {
    val outerObservableValue = this
    return object : ObjectBinding<Inner>() {
        init {
            super.bind(outerObservableValue)

            outerObservableValue.value?.let { outerObject ->
                val innerObservableValue = function(outerObject)
                super.bind(innerObservableValue)
            }

            outerObservableValue.addListener { _, oldOuterObject, newOuterObject ->
                oldOuterObject?.let {
                    val oldInnerObservableValue = function(it)
                    super.unbind(oldInnerObservableValue)
                }
                newOuterObject?.let {
                    val newInnerObservableValue = function(it)
                    super.bind(newInnerObservableValue)
                }
            }
        }

        override fun computeValue(): Inner? = outerObservableValue.value?.let { outerObject ->
            val innerObservableValue = function(outerObject)
            val innerObject = innerObservableValue.value
            innerObject
        }
    }
}

class Tab(titleValue: String) {
    val titleProperty = SimpleStringProperty(titleValue)
}

class Window {
    val tabProperty = SimpleObjectProperty<Tab>()
}

class HelloWorld : Application() {
    val activeWindowProperty = SimpleObjectProperty<Window>()

    override fun start(primaryStage: Stage) {
        var i = 0

        primaryStage.title = "Hello World!"
        val btn = Button()
        btn.text = "Say 'Hello World'"
        btn.onAction = EventHandler<ActionEvent> {
            when (i) {
                0 -> activeWindowProperty.value = Window()
                1 -> activeWindowProperty.value.tabProperty.value = Tab("hello")
                5 -> activeWindowProperty.value = Window()
                6 -> activeWindowProperty.value.tabProperty.value = Tab("hello2")
                else -> activeWindowProperty.value.tabProperty.value.titleProperty.value = "$i"
            }
            ++i
        }

        val root = VBox()
        root.children.add(btn)
        primaryStage.scene = Scene(root, 300.0, 250.0)
        primaryStage.show()

        root.children.add(Text().apply {
            textProperty().bind(activeWindowProperty
                    .let(Window::tabProperty)
                    .let(Tab::titleProperty))
        })
    }
}

fun main(args: Array<String>) {
    Application.launch(HelloWorld::class.java, *args)
}
