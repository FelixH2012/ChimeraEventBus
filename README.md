
# EventSystem

A simple event system, with priorities and caching
## Usage/Examples

Registering an event

```java
import com.felix.chimera.base.implementation.event.Event;

public class ExampleEvent extends Event {

}
```

Initialisation in the main class of your client

```java
public class ClientMain {

    private static ClientMain instance;

    public EventManager eventManager;

    public ClientMain() {
       this.eventManager = new EventManager();
    }

    public static ClientMain instance() {
        if (instance == null) {
            instance = new ClientMain();
        }
        return instance;
    } 
}
```

Triggering an event, for example a tick event in the Minecraft - Main klass

```java
   public void runTick() throws IOException {
      ClientMain.instance().eventManager().triggerEvent(new ExampleEvent());
   }
}
```
Registering the events in the module class



```java
     public void callModule(boolean state) {
        this.moduleToggleState = state;
        if (state) {
             ClientMain.instance().eventManager().subscribe(this);
            enable();
        } else {
             ClientMain.instance().eventManager().unsubscribe(this);
            disable();
        }
    }
}
```

Use of the event in a module

```java
        private final EventManager.EventListener<ExampleEvent> exampleEventEventListener = e -> {

    };
}
```
