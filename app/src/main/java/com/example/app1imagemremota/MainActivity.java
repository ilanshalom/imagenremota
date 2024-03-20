package com.example.app1imagemremota;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private int i = 1;
    private ImageView glbImageView = null;
    private TextView glbTextView = null;
    private String glbNomeImagem = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        glbImageView = findViewById(R.id.imageView1);
        glbTextView = findViewById(R.id.textView);
        if(checkInternetConection()) {
            glbNomeImagem = "Recebeu do servidor fig1.png";
            glbTextView.setText("Figura (local) inicial, aguarde...");
            new DownloadImage().execute(("http://www.mfpledon.com.br/fig1.png"));
        }
        else {
            glbImageView.setImageResource(R.drawable.error);
            glbTextView.setText("Seu dispositivo não tem conexão com Internet.");
        }
    }

    public boolean checkInternetConection() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        return (isWifiConn || isMobileConn);
    }

    public void trocarImagem(View v) {
        i++;
        if (i == 9) i = 1;
        glbNomeImagem = "Recebeu do servidor fig" + i + ".png";
        new DownloadImage().execute(("http://www.mfpledon.com.br/fig" + i + ".png").trim());
    }

    private class DownloadImage extends AsyncTask<String, Void, String> {
        String respostaHttp = "";
        HttpURLConnection conn = null;
        InputStream in = null;
        ByteArrayOutputStream bos = null;
        Bitmap bmp;
        byte[] buffer = null;

        @Override
        protected String doInBackground(String... params) {
            //params[0] é a URL do servidor, recebida no parâmetro acima
            try {
                return downloadImage(params[0]); // executa a solicitação HTTP (próximo slide)
            } catch (IOException e) {
                return "Error com a URL";
            }
        }

        @Override
        //onPostExecute processa o resultado da AsyncTask (ou seja, processa a resposta do servidor)
        protected void onPostExecute(String result) {
            byte[] figBytes = null;
            if (result.indexOf("Erro") == 0) {
                glbImageView.setImageResource(R.drawable.error);
                glbTextView.setText(result);
            } else {
                try {
                    figBytes = bos.toByteArray();
                    bmp = BitmapFactory.decodeByteArray(figBytes, 0, figBytes.length);
                    glbImageView.setImageBitmap(bmp);
                    glbTextView.setText(glbNomeImagem);
                } catch (Exception errmem2) {
                    glbImageView.setImageResource(R.drawable.error);
                    glbTextView.setText("Erro 1, tentando carregar a imagem, verifique conexão com Internet. ");
                } finally { //liberando memória
                    figBytes = null;
                    bmp = null;
                    try {
                       if(bos!=null) bos.close();
                    } catch (Exception ee2) {
                        glbImageView.setImageResource(R.drawable.error);
                        glbTextView.setText("Erro 2, tentando carregar a imagem, verifique conexão com Internet. ");
                    }
                }
            }
        }

        private String downloadImage(String myurl) throws IOException {
            int len;
            try {
                URL u = new URL(myurl);
                conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000); // 5 segundos de timeout
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                in = conn.getInputStream();
                bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                while ((len = in.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                respostaHttp = "Ok";
                Log.d("Error", "OK, no error found");
            } catch (Exception ex) {
                respostaHttp = "Erro de conexão com Internet. ";
                Log.d("Error", "Erro de conexão com o server - " + ex.getMessage());
            } finally {
                if (in != null) in.close();
            }
            return respostaHttp;
        }
    }

}