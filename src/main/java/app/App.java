package app;

import app.model.TaskRepository;
import app.ui.TaskUIHtmx_;
import io.jooby.Jooby;
import io.jooby.handlebars.HandlebarsModule;
import io.jooby.hibernate.validator.HibernateValidatorModule;
import io.jooby.htmx.HtmxErrorHandler;
import io.jooby.htmx.HtmxModule;
import io.jooby.htmx.HtmxResponse;
import io.jooby.netty.NettyServer;

import java.nio.file.Paths;
import java.util.Map;

public class App extends Jooby {

  {
    var views = Paths.get("views");

    /* Handlebars */
    install(new HandlebarsModule(views));

    /* HTMX */
    HtmxErrorHandler globalErrorHandler = (ctx, cause, status) ->
      HtmxResponse.empty(status)
              .addOob("toast.hbs", Map.of(
                      "message", status.reason() + ": " + cause.getMessage(),
                      "isError", true
              ))
    ;
    install(new HtmxModule(globalErrorHandler));

    /* Jakarta validation: Optional */
    install(new HibernateValidatorModule());

    /* Install generated routes: */
    mvc(new TaskUIHtmx_(new TaskRepository()));
  }

  public static void main(final String[] args) {
    runApp(args, new NettyServer(), App::new);
  }
}
