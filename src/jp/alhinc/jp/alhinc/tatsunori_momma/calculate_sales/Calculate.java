package jp.alhinc.jp.alhinc.tatsunori_momma.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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

		String directory = args[0];
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			System.exit(1);
		}

		HashMap<String ,String> branchNameMap = new HashMap<String ,String>(); //支店コード、支店名
		HashMap<String,String> productNameMap = new HashMap<String ,String>();
		LinkedHashMap<String, Long> branchSaleMap = new LinkedHashMap<String, Long>(); //支店コード、売り上げ
		LinkedHashMap<String, Long> productSaleMap = new LinkedHashMap<String,Long>();

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
					System.exit(1);
				}
			}
			br.close();
		}
		catch(IOException e) {
			System.out.println("支店定義ファイルが存在しません");
			System.exit(1);
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
					productSaleMap.put(temp[0], 0l);
				}
				else {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					System.exit(1);
				}
			}
			br.close();
		}
		catch(IOException e) {
			System.out.println("商品定義ファイルが存在しません");
			System.exit(1);
		}


		//売り上げファイルの読み込み
		ArrayList <String>rcdList = new ArrayList<String>();

		File targetDir = null;
		targetDir = new File(directory);
		String[] fileList = targetDir.list();

		//正規表現で抽出する
		String regex = "^[0-9]{8}\\.rcd$";
		for(int i = 0; i < fileList.length; i++) {
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(fileList[i]);
			if (m.find()) {
				rcdList.add(fileList[i]);
			}
		}

		serialNumberCheck(rcdList);

		//支店商品集計
		try{
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
				//要素が三個でない場合
				if(saleTemp.size() != 3) {
					System.out.println(rcdList.get(i)+ "のフォーマットが不正です");
					System.exit(1);
				}
				//支店に該当があるかチェック
				if(!(branchNameMap.containsKey(saleTemp.get(0)))){
					System.out.println(rcdList.get(i) + "の支店コードが不正です");
					System.exit(1);
				}
				//商品に該当があるかチェック
				if(!(productNameMap.containsKey(saleTemp.get(1)))){
					System.out.println(rcdList.get(i) + "の商品コードが不正です");
					System.exit(1);
				}
				//支店商品合計
				branchSaleMap.put(saleTemp.get(0) , branchSaleMap.get(saleTemp.get(0)) + Long.parseLong(saleTemp.get(2)));
				productSaleMap.put(saleTemp.get(1) , productSaleMap.get(saleTemp.get(1)) + Long.parseLong(saleTemp.get(2)));

				//10桁を超えたらループを抜けて読み込み中断
				if (branchSaleMap.get(saleTemp.get(0)) > 1000000000) {
					System.out.println("合計金額が十桁を超えました");
					System.exit(1);
				}
				//10桁を超えたらループを抜けて読み込み中断
				if (productSaleMap.get(saleTemp.get(1)) > 1000000000) {
					System.out.println("合計金額が十桁を超えました");
					System.exit(1);
				}
				//saleTempを空にする
				saleTemp.clear();
				br.close();
			}
		}
		catch(IOException e) {
			System.out.println("売り上げファイルが存在しません");
			System.exit(1);
		}

		branchSaleMap = sortSaleLinkedMap(branchSaleMap);
		productSaleMap = sortSaleLinkedMap(productSaleMap);

		//結果ファイルの作成

		outputFile("branch", directory , branchNameMap, branchSaleMap);
		outputFile("commodity", directory , productNameMap, productSaleMap);

	}

   public static void serialNumberCheck(ArrayList <String>rcdList){
		ArrayList <Integer>rcdListInt = new ArrayList<Integer>();
		for(String r : rcdList) {
			rcdListInt.add(Integer.parseInt(r.substring(0,8)));
		}
		//rcdListの連番チェック
		for(int i = 0 ; i < rcdListInt.size(); i++){
			if (rcdListInt.get(i) != i + 1 ) {
				System.out.println("ファイル名が連番になっていません");
				System.exit(1);
			}
		}
   }

   /* ソートされたリンクドハッシュマップを返すメソッド
    * @param HashMap
    * @return HashMap
    */
   public static LinkedHashMap<String,Long> sortSaleLinkedMap(LinkedHashMap<String, Long> SaleMap){
	   ArrayList<Map.Entry<String,Long>> SaleList = new ArrayList<Map.Entry<String,Long>>(SaleMap.entrySet());
	   Collections.sort(SaleList, new Comparator<Map.Entry<String,Long>>(){
			@Override
			public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2 ) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
	   });

	   LinkedHashMap<String,Long> resultMap = new LinkedHashMap<String,Long>();
	   for (Entry<String,Long> entry : SaleList) {
			resultMap.put(entry.getKey(), entry.getValue());
	   }
	   return resultMap;
   }

   /*
    *
    */
   public static void outputFile(String name,String directory,HashMap<String,String> NameMap, LinkedHashMap<String,Long>SaleMap){
	   File file = new File(directory, name +".out");

		try{
			if (!(file.exists())){
				file.createNewFile();
				System.out.println("ファイルの作成に成功しました");
			}
			if(checkRockFile(file)){
				FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				//書き込み
				for (Entry<String ,Long> entry : SaleMap.entrySet()) {
					pw.println(entry.getKey() + "," + NameMap.get(entry.getKey()) + "," + entry.getValue());
				}
				pw.close();
			}
		}
		catch(IOException e){
			  System.out.println(e);
		}
   }
   public static boolean checkRockFile(File file){
	   if(file.isFile() && file.canWrite()){
		   return true;
	   }
	   return false;
   }
}