package bsu.fpmi.chat.proccesor;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static bsu.fpmi.chat.util.ServletUtil.APPLICATION_JSON;
import static bsu.fpmi.chat.util.ServletUtil.UTF_8;

/**
 * Created by gtrubach on 11.05.2015.
 */
public final class AsyncProcessor {
    private final static Queue<AsyncContext> storage = new ConcurrentLinkedQueue<>();

    public static void notifyAllClients(String data) {
        for (AsyncContext asyncContext : storage) {
            try {
                asyncContext.getResponse().setContentType(APPLICATION_JSON);
                asyncContext.getResponse().setCharacterEncoding(UTF_8);
                final PrintWriter writer = asyncContext.getResponse().getWriter();
                writer.println(data);
                writer.flush();
                asyncContext.complete();
            } catch (IOException e) {
            } finally {
                storage.remove(asyncContext);
            }
        }
    }

    public static void addAsyncContext(final AsyncContext context) {
        context.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                removeAsyncContext(context);
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                removeAsyncContext(context);
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                removeAsyncContext(context);
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
            }
        });
        storage.add(context);
    }

    private static void removeAsyncContext(AsyncContext context) {
        storage.remove(context);
    }
}