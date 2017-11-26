package com.example.admin.cerberus;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.thaonguyen.WSManager.*;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import static org.java_websocket.WebSocket.READYSTATE.NOT_YET_CONNECTED;

public class WSManagerService extends Service {
    private final static String URI = "ws://nfc.ddns.net:8080";
    private final IBinder binder = new LocalWeatherBinder();

    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    private WebSocketClient webSocketClient;

    public IOnOpenWSManager getOnOpenCallback() {
        return onOpenCallback;
    }

    private IOnOpenWSManager onOpenCallback;
    private IOnCloseWSManager onCloseCallback;
    private IOnMessageWSManager onMessageCallback;
    private IOnErrorWSManager onErrorCallback;

    public void setOnOpenCallback(IOnOpenWSManager onOpenCallback) {
        this.onOpenCallback = onOpenCallback;
    }

    public IOnCloseWSManager getOnCloseCallback() {
        return onCloseCallback;
    }

    public void setOnCloseCallback(IOnCloseWSManager onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    public IOnMessageWSManager getOnMessageCallback() {
        return onMessageCallback;
    }

    public void setOnMessageCallback(IOnMessageWSManager onMessageCallback) {
        this.onMessageCallback = onMessageCallback;
    }

    public IOnErrorWSManager getOnErrorCallback() {
        return onErrorCallback;
    }

    public void setOnErrorCallback(IOnErrorWSManager onErrorCallback) {
        this.onErrorCallback = onErrorCallback;
    }

    public class LocalWeatherBinder extends Binder {
        public WSManagerService getService() {
            return WSManagerService.this;
        }
    }

    public WSManagerService() {
        try {
            webSocketClient = new WebSocketClient(new URI(URI)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    if (onOpenCallback != null) {
                        onOpenCallback.onOpen();
                    }
                }

                @Override
                public void onMessage(String s) {
                    if (onMessageCallback != null) {
                        onMessageCallback.onMessage(s);
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    if (onCloseCallback != null) {
                        onCloseCallback.onClose();
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (onErrorCallback != null) {
                        onErrorCallback.onError(e);
                    }
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void connect() {
        switch (webSocketClient.getReadyState()) {
            case NOT_YET_CONNECTED:
                webSocketClient.connect();
                break;
            case CLOSED:
                try {
                    webSocketClient = new WebSocketClient(new URI(URI)) {
                        @Override
                        public void onOpen(ServerHandshake serverHandshake) {
                            if (onOpenCallback != null) {
                                onOpenCallback.onOpen();
                            }
                        }

                        @Override
                        public void onMessage(String s) {
                            if (onMessageCallback != null) {
                                onMessageCallback.onMessage(s);
                            }
                        }

                        @Override
                        public void onClose(int i, String s, boolean b) {
                            if (onCloseCallback != null) {
                                onCloseCallback.onClose();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            if (onErrorCallback != null) {
                                onErrorCallback.onError(e);
                            }
                        }
                    };
                    webSocketClient.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public WebSocket.READYSTATE getState() {
        return webSocketClient.getReadyState();
    }

    public void disconnect() {
        WebSocket.READYSTATE state = webSocketClient.getReadyState();
        if (state == WebSocket.READYSTATE.OPEN) {
            webSocketClient.close();
        }
    }

    public void sendMessage(String message) {
        webSocketClient.send(message);
    }
}
