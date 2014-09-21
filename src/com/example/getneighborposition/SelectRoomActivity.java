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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.*;

import com.example.getneighborposition.R.id;

public class SelectRoomActivity extends Activity {
	// アプリケーション名
	private static final String APPNAME = "Chat";
	// 接続先サーバーのホスト名
	private static final String HOST = "192.168.11.2";
	// 接続先ポート番号
	private static final int PORT = 2815;
	
	
	private static final int GET_ROOM = 0;
	private static final int NEW_ROOM = 1;
	// このアプリケーションのクライアントソケット
	//private Socket socket;

	// 現在入室中のチャットルーム名
	//private String roomName;

	// 以下、コンポーネント
	// private JList roomList;
	// private JList userList;
	private List<String> roomList; // チャットルームのリスト
	private List<String> userList; // 現在入室中のチャットルームのユーザー
	// メッセージ受信監視用スレッド
	private Thread threadMonitorMes;
	// 受信テキスト（制御メッセージも含む）
	private TextView txRecv;

	// 表示するルームリスト
	private ArrayAdapter<String> roomListAdapter;

	public ChatObject chatobj;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_chat_room);
		
		chatobj = new ChatObject();
		
		Toast.makeText(SelectRoomActivity.this, "setcontent view。",
				Toast.LENGTH_LONG).show();
		// サーバにコネクト
		connectServer();
		
		// メッセージ受信監視用のスレッドを生成してスタートさせる
		threadMonitorMes = new Thread() {
			// サーバに送られているメッセージの定期監視
			public void run() {
				try {
					InputStream input = chatobj.socket.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(input));
					while (!chatobj.socket.isClosed()) {
						String line = reader.readLine();

						String[] msg = line.split(" ", 2);
						String msgName = msg[0];
						String msgValue = (msg.length < 2 ? "" : msg[1]);

						reachedMessage(msgName, msgValue);
					}
				} catch (Exception err) {
					Toast.makeText(SelectRoomActivity.this, "サーバからの通信に異常がある模様。",
							Toast.LENGTH_LONG).show();
				}
			}
		};
		// メッセージ受信監視用のスレッドを生成してスタートさせる
		
		threadMonitorMes.start();
		// 現在の部屋を取得する
		sendMessage("getRooms");

		// ルームを新規で作成する場合
		findViewById(R.id.butSend).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chatobj.roomName = ((EditText) findViewById(R.id.txtSend))
						.getText().toString();
				
				//sendMessage("addRoom " + chatobj.roomName);
				//enteredRoom(roomName);
				//sendMessage("getUsers " + chatobj.roomName);
				// ソケット，ルームを与えてintentする
			}

		});
	}

	// サーバーに接続する
	public void connectServer() {
		try {
			chatobj.socket = new Socket(HOST, PORT);
			Toast.makeText(SelectRoomActivity.this, "サーバに接続しました。",
					Toast.LENGTH_LONG).show();

		} catch (Exception err) {
			Toast.makeText(SelectRoomActivity.this, "サーバに接続できません。",
					Toast.LENGTH_LONG).show();

		}
	}

	// サーバーから切断する
	public void close() throws IOException {
		sendMessage("close");
		chatobj.socket.close();
		Toast.makeText(SelectRoomActivity.this, "サーバとの接続が切れました。",
				Toast.LENGTH_LONG).show();

	}

	// メッセージをサーバーに送信する(ルームの取得のみ)
	public void sendMessage(String msg) {
		try {

			
			PrintWriter writer = new PrintWriter(chatobj.socket.getOutputStream(), true);

			/*String sendTxt = ((EditText) findViewById(R.id.txtSend)).getText()
					.toString();*/
			writer.println(msg);
			writer.flush();
		} catch (UnknownHostException e) {

			Toast.makeText(SelectRoomActivity.this, "エラー" + e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(SelectRoomActivity.this, "エラー" + e,
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	// サーバーから送られてきたメッセージの処理
	public void reachedMessage(String name, String value) {
		// ルームリスト
		roomListAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		
		// チャットルームのリストに変更が加えられた
		if (name.equals("rooms")) {
			if (value.equals("")) {
				roomListAdapter.add("現在，ルームがありません。");
				ListView listView = (ListView) findViewById(id.room_list);
				// アダプターを設定
				listView.setAdapter(roomListAdapter);
			} else {// 部屋リストの取得
				String[] rooms = value.split(" ");

				// リストにルームを追加する
				for (int i = 0; i < rooms.length; i++) {
					roomListAdapter.add(rooms[i]);
				}
				
				
				ListView listView = (ListView) findViewById(id.room_list);

				// アダプターを設定
				listView.setAdapter(roomListAdapter);

				// リストビューのアイテムがクリックされた時に呼び出されるコールバックリスナーを登録
				listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						ListView listView = (ListView) parent;
						// クリックされたアイテムを取得します
						String item = (String) listView
								.getItemAtPosition(position);
						Toast.makeText(SelectRoomActivity.this, item,
								Toast.LENGTH_LONG).show();
						
						// ここでチャットルームを選択してintentする
						// --以下修正対象
						// 受信テキストと表示エリアを紐付
						//txRecv = (TextView) findViewById(id.txtRecv);

						// コンポーネントの状態を退室状態で初期化
						//exitedRoom();
						//sendMessage("enterRoom " + roomName);
						// 部屋を取得する
						//sendMessage("getRooms");

						
					}
				});
				
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
		chatobj.roomName = roomName;
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
		chatobj.roomName = null;
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
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}*/

}
