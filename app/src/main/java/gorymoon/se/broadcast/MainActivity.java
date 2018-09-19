package gorymoon.se.broadcast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private InputFilter hexFilter = new InputFilter() {
        private String validText = "0123456789ABCDEFabcdef";

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source != null && (!validText.contains(("" + source)))) {
                return source.toString().replaceAll("[^" + validText + "]", "");
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText mask = findViewById(R.id.ip);
        final EditText data = findViewById(R.id.data);
        List<InputFilter> list = new ArrayList<>();
        list.add(hexFilter);
        list.add(new InputFilter.LengthFilter(2));
        list.add(new InputFilter.AllCaps());
        data.setFilters(list.toArray(new InputFilter[0]));

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String s = data.getText().toString();
                    if (s.length() < 2 || s.length() > 2) {
                        Toast.makeText(MainActivity.this, "Invalid hex code length", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int hex;
                    try {
                        hex = Integer.parseInt(s, 16);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Invalid hex code", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    broadcast(ByteBuffer.allocate(4).putInt(hex).array(), InetAddress.getByName(mask.getText().toString()));
                    Toast.makeText(MainActivity.this, "Sent broadcast", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void broadcast(final byte[] broadcastMessage, final InetAddress address) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    socket.connect(address, 4446);

                    DatagramPacket packet = new DatagramPacket(broadcastMessage, broadcastMessage.length);
                    socket.send(packet);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }
}
