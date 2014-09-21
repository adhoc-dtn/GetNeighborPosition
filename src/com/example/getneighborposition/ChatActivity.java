package com.example.getneighborposition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.util.*;

import com.example.getneighborposition.R.id;

public class ChatActivity extends Activity {
	// アプリケーション名
	private static final String APPNAME = "Chat";
	// 接続先サーバーのホスト名
	private static final String HOST = "localhost";
	// 接続先ポート番号
	private static final int PORT = 2815;
	// このアプリケーションのクライアントソケット
	private Socket socket;

	// 現在入室中のチャットルーム名
	private String roomName;

	// 以下、コンポーネント
	// private JList roomList;
	// private JList userList;
	private List<String> roomList; // チャットルームのリスト
	private List<String> userList; // 現在入室中のチャットルームのユーザー
	// メッセージ受信監視用スレッド
	private Thread threadMonitorMes;
	// 受信テキスト（制御メッセージも含む）
	private TextView txRecv;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_mode);

		// 受信テキストと表示エリアを紐付
		txRecv = (TextView) findViewById(id.txtRecv);

		// コンポーネントの状態を退室状態で初期化
		exitedRoom();

		// サーバにコネクト
		connectServer();
		// メッセージ受信監視用のスレッドを生成してスタートさせる
		threadMonitorMes = new Thread() {
			// サーバに送られているメッセージの定期監視
			public void run() {
				try {
					InputStream input = socket.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(input));
					while (!socket.isClosed()) {
						String line = reader.readLine();

						String[] msg = line.split(" ", 2);
						String msgName = msg[0];
						String msgValue = (msg.length < 2 ? "" : msg[1]);

						reachedMessage(msgName, msgValue);
					}
				} catch (Exception err) {
				}
			}
		};

		threadMonitorMes.start();

		// 現在の部屋を取得する
		sendMessage("getRooms");

		findViewById(R.id.butSend).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String sendTxt =
                        ((EditText)findViewById(R.id.txtSend)).getText().toString();
				sendMessage(sendTxt);
			}

		});
	}

	// サーバーに接続する
	public void connectServer() {
		try {
			socket = new Socket(HOST, PORT);
			// msgTextArea.append(">サーバーに接続しました\n");
		} catch (Exception err) {
			// msgTextArea.append("ERROR>" + err + "\n");
		}
	}

	// サーバーから切断する
	public void close() throws IOException {
		// sendMessage("close");
		socket.close();
	}

	// メッセージをサーバーに送信する
	public void sendMessage(String msg) {
		try {

			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

			String sendTxt = ((EditText) findViewById(R.id.txtSend)).getText()
					.toString();
			writer.println(sendTxt);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// サーバーから送られてきたメッセージの処理
	public void reachedMessage(String name, String value) {

		// チャットルームのリストに変更が加えられた
		if (name.equals("rooms")) {
			if (value.equals("")) {
				// roomList.setModel(new DefaultListModel());
			} else {
				// String[] rooms = value.split(" ");
				// roomList.setListData(rooms);
			}
		}
		// ユーザーが入退室した
		else if (name.equals("users")) {
			if (value.equals("")) {
				// userList.setModel(new DefaultListModel());
			} else {
				// String[] users = value.split(" ");
				// userList.setListData(users);
			}
		}
		// メッセージが送られてきた
		else if (name.equals("msg")) {

			txRecv.setText(value + "\n");
		}
		// 処理に成功した
		else if (name.equals("successful")) {
			if (value.equals("setName"))
				txRecv.setText(">名前を変更しました\n");

		}
		// エラーが発生した
		else if (name.equals("error")) {
			txRecv.setText("ERROR>" + value + "\n");
		}
	}

	// 部屋に入室している状態のコンポーネント設定
	private void enteredRoom(String roomName) {
		this.roomName = roomName;
		/*
		 * setTitle(APPNAME + " " + roomName);
		 * 
		 * msgTextField.setEnabled(true); submitButton.setEnabled(true);
		 * 
		 * addRoomButton.setEnabled(false); enterRoomButton.setText("退室");
		 * enterRoomButton.setActionCommand("exitRoom");
		 */
	}

	// 部屋に入室していない状態のコンポーネント設定
	private void exitedRoom() {
		roomName = null;
		/*
		 * setTitle(APPNAME);
		 * 
		 * msgTextField.setEnabled(false); submitButton.setEnabled(false);
		 * 
		 * addRoomButton.setEnabled(true); enterRoomButton.setText("入室");
		 * enterRoomButton.setActionCommand("enterRoom"); userList.setModel(new
		 * DefaultListModel());
		 */
	}

	// オプションメニュー作成
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	/*// オプションメニュークリック時
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			reloadTimeLine();
			return true;

		case R.id.menu_tweet:
			Intent intent = new Intent(this, TweetActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

}
