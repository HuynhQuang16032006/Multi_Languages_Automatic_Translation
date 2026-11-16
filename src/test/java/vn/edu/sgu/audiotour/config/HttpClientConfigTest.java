package vn.edu.sgu.audiotour.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.web.client.ResourceAccessException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpClientConfigTest {
    @Test
    @Timeout(value = 40, unit = TimeUnit.SECONDS)
    void timesOutWhenLocalServerNeverResponds() throws Exception {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            CompletableFuture<Socket> accepted = CompletableFuture.supplyAsync(() -> {
                try {
                    return server.accept();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            try {
                var client = new HttpClientConfig().restClientBuilder().build();
                ResourceAccessException error = assertThrows(ResourceAccessException.class, () -> client.get()
                        .uri("http://127.0.0.1:" + server.getLocalPort() + "/")
                        .retrieve().body(String.class));
                assertInstanceOf(SocketTimeoutException.class, error.getCause());
            } finally {
                if (accepted.isDone() && !accepted.isCompletedExceptionally()) {
                    accepted.join().close();
                }
            }
        }
    }
}
