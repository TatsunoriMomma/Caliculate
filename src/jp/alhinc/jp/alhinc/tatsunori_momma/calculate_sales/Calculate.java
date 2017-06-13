package jp.alhinc.jp.alhinc.tatsunori_momma.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//ファイルディレクトリ:C:\\java

public class Calculate {



	public static void main(String[] args) {
		/*
		//ディレクトリを標準入力で受け取る
		System.out.println("ディレクトリを入力してください。");
		InputStreamReader in = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(in);
		*/
		String directory = "C:\\java";
		/*
		try{
			String name = reader.readLine();
			directory = name;
		}
		catch(IOException e){
			System.out.println(e);
		}
		*/

		HashMap<String ,String> branchNameMap = new HashMap<String ,String>(); //支店コード、支店名
		HashMap<String,String> productNameMap = new HashMap<String ,String>();
		LinkedHashMap<String, Long> branchSaleMap = new LinkedHashMap<String, Long>(); //支店コード、売り上げ
		HashMap<String, Long> productSaleMap = new HashMap<String,Long>();

		//支店定義ファイルの格納
		try{
			File file = new File(directory , "branch.lst");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String a;
			String[] temp;

			while((a = br.readLine()) != null) {
				temp = a.split(",",-1);
				//支店定義フォーマットの判別式
				if(temp.length == 2 && temp[0].matches("[0-9]{3}")) {
					branchNameMap.put(temp[0],temp[1]);
					branchSaleMap.put(temp[0],0l);
				} else {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					break;
				}
			}
			System.out.println(branchNameMap);
			br.close();
		}
		catch(IOException e) {
			System.out.println(e);
			System.out.println("支店定義ファイルが存在しません");
		}

		//商品定義ファイルの格納
		try{
			File file = new File(directory ,"commodity.lst");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String a;
			String[] temp;

			while((a = br.readLine()) != null) {
				temp = a.split(",",-1);
				//商品定義フォーマットの判別式
				if(temp.length == 2 && temp[0].matches("^[A-Z]{3}[0-9]{5}$")) {
					productNameMap.put(temp[0],temp[1]);
				}
				else {
					System.out.println("商品定義ファイルのフォーマットが不正です");
				}
			}
			System.out.println(productNameMap);
			br.close();
		}
		catch(IOException e) {
			System.out.println(e);
			System.out.println("商品定義ファイルが存在しません");
		}


		//売り上げファイルの読み込み
		ArrayList <String>rcdList = new ArrayList<String>();

		File targetDir = null;
		targetDir = new File(directory);

		String[] fileList = targetDir.list();
		System.out.println(Arrays.toString(fileList));

		//正規表現で抽出する
		//.rcdの拡張子マッチング、ファイル名は数字のみ8桁
		String regex = "^[0-9]{8}\\.rcd$";
		for(int i = 0; i < fileList.length; i++) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(fileList[i]);
			if (m.find()) {
				rcdList.add(fileList[i]);
			}
		}

		SerialNumberCheck(rcdList);

		try{
			//rcdファイルをひとつずつ処理する
			int i ; // i + 1番目のrcdファイルを処理中
			ArrayList<String> saleTemp;
			saleTemp = new ArrayList<String>(); //[0]=支店コード[1]=商品コード[2]=金額
			for(i = 0; i < rcdList.size(); i++) {
				File file = new File(directory ,rcdList.get(i));
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);

				String a;
				while((a = br.readLine()) != null){
					saleTemp.add(a);
				}
				//要素が三個でない場合-メソッド化してエラーチェックにする
				if(saleTemp.size() != 3) {
					System.out.println(i + 1 + "のフォーマットが不正です");
					break;
				}

				//支店合計
				if(branchNameMap.containsKey(saleTemp.get(0))){
					branchSaleMap.put(saleTemp.get(0) , branchSaleMap.get(saleTemp.get(0)) + Integer.parseInt(saleTemp.get(2)));
				}
				//10桁を超えたらループを抜けて読み込み中断
				if (branchSaleMap.get(saleTemp.get(0)) > 1000000000) {
					System.out.println("合計金額が十桁を超えました");
					break;
				}
				//saleTempを空にする
				saleTemp.clear();
				br.close();
			}

		}
		catch(IOException e) {
			System.out.println(e);
			System.out.println("売り上げファイルが存在しません");
		}
		catch(NumberFormatException e){
			System.out.println(e);
		}

		branchSaleMap = sortSaleList(branchSaleMap);

		//支店の出力（確認用）
		for(String key : branchSaleMap.keySet()) {
			System.out.println(key + "," + branchNameMap.get(key) +"," +branchSaleMap.get(key) );
		}


		//branch.outの作成
		File file = new File(directory,"branch.out");
			try{
			  if (file.createNewFile()){
				System.out.println("ファイルの作成に成功しました");
			  }else{
				System.out.println("ファイルの作成に失敗しました");
			  }
			  FileWriter fw = new FileWriter(file, true);
			  BufferedWriter bw = new BufferedWriter(fw);
			  PrintWriter pw = new PrintWriter(bw);
			  //書き込み
			  for (Entry<String ,Long> entry : branchSaleMap.entrySet()) {
					pw.println(entry.getKey() + "," + branchNameMap.get(entry.getKey()) + "," + entry.getValue());
			  }
			  //PrintWriterオブジェクトをクローズ
			  pw.close();
			}
			catch(IOException e){
			  System.out.println(e);
			}

	}

   public static void SerialNumberCheck(ArrayList <String>rcdList){
	   //rcdListIntへキャストしてコピー
		ArrayList <Integer>rcdListInt = new ArrayList<Integer>();
		for(String r : rcdList) {
			rcdListInt.add(Integer.parseInt(r.substring(0,8)));
		}
		//rcdListの連番チェック
		for(int i = 0 ; i < rcdListInt.size(); i++){
			if (rcdListInt.get(i) != i + 1 ) {
				System.out.println("ファイル名が連番になっていません");
				System.exit(0);
			}
		}
   }
   /* ソートされたリンクドハッシュマップを返すメソッド
    * @param HashMap
    * @return HashMap
    */
   public static LinkedHashMap<String,Long> sortSaleList(LinkedHashMap<String, Long> branchSaleMap){
	   ArrayList<Map.Entry<String,Long>> BranchSaleList = new ArrayList<Map.Entry<String,Long>>(branchSaleMap.entrySet());
	   Collections.sort(BranchSaleList, new Comparator<Map.Entry<String,Long>>(){
			@Override
			public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2 ) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
	   });

	   LinkedHashMap<String,Long> resultMap = new LinkedHashMap<String,Long>();
	   for (Entry<String,Long> entry : BranchSaleList) {
			resultMap.put(entry.getKey(), entry.getValue());
	   }
	   return resultMap;
   }
   /*
   集計を配列にした場合；
   //rcdファイルをひとつずつ処理する
	int i ; // i + 1番目のrcdファイルを処理中
	String[]  saleTemp;
	saleTemp = new String[3]; //[0]=支店コード[1]=商品コード[2]=金額
	for(i = 0; i < rcdList.size(); i++) {
		File file = new File(directory ,rcdList.get(i));
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		//ファイルの読み込み、ここでファイルの中身が四桁以上あるときのエラーをやる
		//try-catchがネストしているのも問題
		String a;
		int count = 0; //count + 1は何行目の処理なのかを示す
		while((a = br.readLine()) != null){
			try{
				saleTemp[count] = a;
				count += 1;
			}
			//4行を超える場合
			catch(ArrayIndexOutOfBoundsException e) {
				System.out.println(e);
				System.out.println( i + 1 + "のフォーマットが不正です");
			}
		}
		//3行ない場合
		for(String tem  : saleTemp){
			if(tem == null) {
				System.out.println(i + 1 + "のフォーマットが不正です");
			}
		}
		//支店合計
		if(branchNameMap.containsKey(saleTemp[0])){
			branchSaleMap.put(saleTemp[0] , branchSaleMap.get(saleTemp[0]) + Integer.parseInt(saleTemp[2]));
		}
		//10桁を超えたらループを抜けて読み込み中断
		if (branchSaleMap.get(saleTemp[0]) > 1000000000) {
			System.out.println("合計金額が十桁を超えました");
			break;
		}
		//saleTempを空にする
		saleTemp[0] = null;
		saleTemp[1] = null;
		saleTemp[2] = null;
		br.close();
   */
}