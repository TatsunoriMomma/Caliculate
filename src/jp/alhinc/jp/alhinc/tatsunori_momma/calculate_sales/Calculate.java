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

//ファイルディレクトリ:C:\\java

public class Calculate {
	public static void main(String[] args) {

		//コマンドライン引数チェック
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		String directory = args[0];


		HashMap<String ,String> branchNameMap = new HashMap<String ,String>(); //支店コード、支店名
		HashMap<String,String> commodityNameMap = new HashMap<String ,String>();
		LinkedHashMap<String, Long> branchSaleMap = new LinkedHashMap<String, Long>(); //支店コード、売り上げ
		LinkedHashMap<String, Long> commoditySaleMap = new LinkedHashMap<String,Long>();

		String filePath1 = directory + File.separator + "branch.lst";
		String filePath2 = directory + File.separator + "commodity.lst";
		//支店定義ファイルの格納
		if(!(inputFile(filePath1,"支店","[0-9]{3}", branchNameMap, branchSaleMap))){
			return;
		}

		//商品定義ファイルの格納
		if(!(inputFile(filePath2,"商品","^[A-Z]{3}[0-9]{5}$", commodityNameMap, commoditySaleMap))){
			return;
		}


		//売り上げファイルの読み込み、ファイル抽出
		ArrayList <String>rcdList = new ArrayList<String>();
		ArrayList <String>fileList = new ArrayList<String>();

		File targetDir = null;
		targetDir = new File(directory);
		File[] targetFiles = targetDir.listFiles();
		for(int i = 0; i < targetFiles.length;i++){
			if(targetFiles[i].isFile()){
				fileList.add(targetFiles[i].getName());
			}
		}
		for(int i = 0; i < fileList.size(); i++) {
			if (fileList.get(i).matches("^[0-9]{8}\\.rcd$")) {
				rcdList.add(fileList.get(i));
			}
		}

		if(!(serialNumberCheck(rcdList))){
			return;
		}

		//支店商品集計
		try{
			int i ; // i + 1番目のrcdファイルを処理中
			ArrayList<String> saleTemp;
			saleTemp = new ArrayList<String>(); //[0]=支店コード[1]=商品コード[2]=金額
			for(i = 0; i < rcdList.size(); i++) {
				File file = new File(directory ,rcdList.get(i));
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				try{
					String a;
					while((a = br.readLine()) != null){
						saleTemp.add(a);
					}
					//要素が三個でない場合
					if(saleTemp.size() != 3) {
						System.out.println(rcdList.get(i)+ "のフォーマットが不正です");
						return;
					}
					//支店に該当があるかチェック
					if(!(branchNameMap.containsKey(saleTemp.get(0)))){
						System.out.println(rcdList.get(i) + "の支店コードが不正です");
						return;
					}
					//商品に該当があるかチェック
					if(!(commodityNameMap.containsKey(saleTemp.get(1)))){
						System.out.println(rcdList.get(i) + "の商品コードが不正です");
						return;
					}
					//支店商品合計
					branchSaleMap.put(saleTemp.get(0) , branchSaleMap.get(saleTemp.get(0)) + Long.parseLong(saleTemp.get(2)));
					commoditySaleMap.put(saleTemp.get(1) , commoditySaleMap.get(saleTemp.get(1)) + Long.parseLong(saleTemp.get(2)));

					//10桁を超えたらループを抜けて読み込み中断
					if (branchSaleMap.get(saleTemp.get(0)) > 999999999l) {
						System.out.println("合計金額が十桁を超えました");
						return;
					}
					//10桁を超えたらループを抜けて読み込み中断
					if (commoditySaleMap.get(saleTemp.get(1)) > 999999999l) {
						System.out.println("合計金額が十桁を超えました");
						return;
					}
					//saleTempを空にする
					saleTemp.clear();
				}
				finally{
					br.close();
				}
			}
		}
		catch(IOException e) {
			System.out.println("売り上げファイルが存在しません");
			return;
		}

		//ソート
		branchSaleMap = sortSaleLinkedMap(branchSaleMap);
		commoditySaleMap = sortSaleLinkedMap(commoditySaleMap);

		//結果ファイルの作成

		if(!(outputFile("branch", directory , branchNameMap, branchSaleMap))){
			return;
		}
		if(!(outputFile("commodity", directory , commodityNameMap, commoditySaleMap))){
			return;
		}

	}

	public static boolean inputFile(String filepass, String name,String regularExpression, HashMap<String,String>NameMap, LinkedHashMap<String,Long>SaleMap){
		File file = new File(filepass);
		if(!(file.exists())){
			System.out.println(name + "定義ファイルが存在しません");
			return false;
		}
		BufferedReader br = null;
		try{
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String a;
			String[] temp;
				while((a = br.readLine()) != null) {
					temp = a.split(",",-1);
					//コードのフォーマットの判別式
					if(temp.length == 2 && temp[0].matches(regularExpression)) {
						NameMap.put(temp[0],temp[1]);
						SaleMap.put(temp[0],0l);
					} else {
						System.out.println(name + "定義ファイルのフォーマットが不正です");
						return false;
					}
				}

		} catch(IOException e) {
			System.out.println(name + "定義ファイルが存在しません");
			return false;
		} finally{
			try{
				br.close();
			}
			catch(IOException e) {
				System.out.println(name + "定義ファイルのフォーマットが不正です");
				return false;
			}

		}
		return true;
	}

	public static boolean serialNumberCheck(ArrayList <String>rcdList){
		ArrayList <Integer>rcdListInt = new ArrayList<Integer>();
		for(String r : rcdList) {
			rcdListInt.add(Integer.parseInt(r.substring(0,8)));
		}
		//rcdListの連番チェック
		for(int i = 0 ; i < rcdListInt.size(); i++){
			if (rcdListInt.get(i) != i + 1 ) {
				System.out.println("ファイル名が連番になっていません");
				return false;
			}
		}
		return true;
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
	public static boolean outputFile(String type,String directory,HashMap<String,String> NameMap, LinkedHashMap<String,Long>SaleMap){
		File file = new File(directory, type +".out");

		try{
			if (!(file.exists())){
				file.createNewFile();
			}
			if(checkFile(file)){
				FileWriter fw = new FileWriter(file, false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);
				try{
					//書き込み
					for (Entry<String ,Long> entry : SaleMap.entrySet()) {
						pw.println(entry.getKey() + "," + NameMap.get(entry.getKey()) + "," + entry.getValue());
					}
				}
				finally{
					pw.close();
				}
			}
		}
		catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}
		return true;
	}
	public static boolean checkFile(File file){
		if(file.isFile() && file.canWrite()){
			return false;
		}
		return true;
	}
}