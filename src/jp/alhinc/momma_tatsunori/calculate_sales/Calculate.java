package jp.alhinc.momma_tatsunori.calculate_sales;

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

public class Calculate {
	public static void main(String[] args) {

		//コマンドライン引数チェック
		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		String directory = args[0];


		HashMap<String, String> branchNameMap = new HashMap<String, String>(); //支店コード、支店名
		HashMap<String, String> commodityNameMap = new HashMap<String, String>();
		LinkedHashMap<String, Long> branchSaleMap = new LinkedHashMap<String, Long>(); //支店コード、売り上げ
		LinkedHashMap<String, Long> commoditySaleMap = new LinkedHashMap<String, Long>();

		String filePath1 = directory + File.separator + "branch.lst";
		String filePath2 = directory + File.separator + "commodity.lst";

		//支店定義ファイルの格納
		if(!(inputFile(filePath1, "支店", "[0-9]{3}", branchNameMap, branchSaleMap))){
			return;
		}

		//商品定義ファイルの格納
		if(!(inputFile(filePath2, "商品", "([0-9]|[A-Z]|[a-z]){8}", commodityNameMap, commoditySaleMap))){
			return;
		}


		//売り上げファイルの読み込み、ファイル抽出
		ArrayList<String>fileList = new ArrayList<String>();
		ArrayList<String>rcdList = new ArrayList<String>();

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
			ArrayList<String>saleTemp;
			saleTemp = new ArrayList<String>(); //[0]=支店コード[1]=商品コード[2]=金額
			for(i = 0; i < rcdList.size(); i++) {
				File file = new File(directory, rcdList.get(i));
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
					branchSaleMap.put(saleTemp.get(0), branchSaleMap.get(saleTemp.get(0)) + Long.parseLong(saleTemp.get(2)));
					commoditySaleMap.put(saleTemp.get(1), commoditySaleMap.get(saleTemp.get(1)) + Long.parseLong(saleTemp.get(2)));

					//10桁を超えたらループを抜けて読み込み中断
					if (branchSaleMap.get(saleTemp.get(0)) > 9999999999L) {
						System.out.println("合計金額が10桁を超えました");
						return;
					}
					//10桁を超えたらループを抜けて読み込み中断
					if (commoditySaleMap.get(saleTemp.get(1)) > 9999999999L) {
						System.out.println("合計金額が10桁を超えました");
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
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		catch(NumberFormatException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		//ソート
		branchSaleMap = sortSaleLinkedMap(branchSaleMap);
		commoditySaleMap = sortSaleLinkedMap(commoditySaleMap);

		//結果ファイルの作成
		if(!(outputFile("branch.out", directory, branchNameMap, branchSaleMap))){
			return;
		}
		if(!(outputFile("commodity.out", directory, commodityNameMap, commoditySaleMap))){
			return;
		}

	}
	/*ファイルを入力する
	 *
	 */
	public static boolean inputFile(String filePass, String definitionName, String regularExpression, HashMap<String, String>nameMap, LinkedHashMap<String, Long>saleMap){
		File file = new File(filePass);
		if(!(file.exists())){
			System.out.println(definitionName + "定義ファイルが存在しません");
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
					nameMap.put(temp[0],temp[1]);
					saleMap.put(temp[0],0l);
				} else {
					System.out.println(definitionName + "定義ファイルのフォーマットが不正です");
					return false;
				}
			}

		} catch(IOException e) {
			System.out.println(definitionName + "定義ファイルが存在しません");
			return false;
		} finally {
			try{
				if(br != null){
					br.close();
				} else {
					return false;
				}
			}
			catch(IOException e) {
				System.out.println(definitionName + "定義ファイルのフォーマットが不正です");
				return false;
			}
		}
		return true;
	}

	public static boolean serialNumberCheck(ArrayList<String>rcdList){
		ArrayList<Integer>rcdListInt = new ArrayList<Integer>();
		for(String r : rcdList) {
			rcdListInt.add(Integer.parseInt(r.substring(0,8)));
		}
		//rcdListの連番チェック
		for(int i = 0 ; i < rcdListInt.size(); i++){
			if (rcdListInt.get(i) != i + 1 ) {
				System.out.println("売上ファイル名が連番になっていません");
				return false;
			}
		}
		return true;
	}

	/* ソートされたリンクドハッシュマップを返すメソッド
	 *
	 * @param saleMap コード、売り上げのmap
	 * @return resultMap
	 */
	public static LinkedHashMap<String, Long>sortSaleLinkedMap(LinkedHashMap<String, Long>saleMap){
		ArrayList<Map.Entry<String, Long>> SaleList = new ArrayList<Map.Entry<String, Long>>(saleMap.entrySet());
		Collections.sort(SaleList, new Comparator<Map.Entry<String, Long>>(){
			@Override
			public int compare(
					Entry<String, Long> entry1, Entry<String, Long> entry2 ) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});

		LinkedHashMap<String, Long> resultMap = new LinkedHashMap<String, Long>();
		for (Entry<String,Long> entry : SaleList) {
			resultMap.put(entry.getKey(), entry.getValue());
		}
		return resultMap;
	}

	/*
	 *
	 */
	public static boolean outputFile(String fileName, String directory, HashMap<String, String>nameMap, LinkedHashMap<String, Long>saleMap){
		File file = new File(directory, fileName);
		PrintWriter pw = null;
		try{
			if (!(file.exists())){
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, false);
			BufferedWriter bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw, false);

			//書き込み
			for (Entry<String, Long> entry : saleMap.entrySet()) {
				pw.println(entry.getKey() + "," + nameMap.get(entry.getKey()) + "," + entry.getValue());
			}
		} catch (IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			if(pw != null){
				pw.close();
			} else{
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}

		}
		return true;
	}
}