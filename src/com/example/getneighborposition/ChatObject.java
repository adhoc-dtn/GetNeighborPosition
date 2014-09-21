package com.example.getneighborposition;

import java.net.Socket;

//ユーザーのくらす定義 本来ここに一連の操作
//（TCPセッション確立，メッセージ送信等）を突っ込みたい
public class ChatObject {
	// TCPソケット
	public Socket socket;
	// 現在入室中のチャットルーム名
	public String roomName;
	//ユーザー名前
	public String userName;
}