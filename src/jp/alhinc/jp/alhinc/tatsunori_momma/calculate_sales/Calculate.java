package jp.alhinc.jp.alhinc.tatsunori_momma.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//ファイルディレクトリ:C:\\java
//商品定義ファイルディレクトリ:



public class Calculate {
    public static void main(String[] args) {
    	//ディレクトリを標準入力で受け取る
    	System.out.println("ディレクトリを入力してください。");
    	InputStreamReader in = new InputStreamReader(System.in);
    	BufferedReader reader = new BufferedReader(in);

    	String directory = "";

    	HashMap<String ,String> BranchMap = new HashMap<String ,String>(); //支店コード、支店名

    	HashMap<String, Integer> BranchSaleSum = new HashMap<String, Integer>(); //支店コード、売り上げ
		HashMap<String, Integer> ProductSaleSum = new HashMap<String,Integer>();

    	//支店定義ファイルの格納
    	try{
    		String name = reader.readLine();
    		directory = name;
		    File file = new File(name , "branch.lst");
		    FileReader fr = new FileReader(file);
		    BufferedReader br = new BufferedReader(fr);

            String a;
            String[] temp;

            while((a = br.readLine()) != null) {
        	    	temp = a.split(",");
        	        //支店定義フォーマットの判別式
        	    	if(temp.length == 2 && temp[0].length() == 3) {
        	            try{

        	                BranchMap.put(temp[0],temp[1]);
        	                BranchSaleSum.put(temp[0],0);
        	            }
        	            catch(java.lang.NumberFormatException e) {
        	            	System.out.println("支店定義ファイルのフォーマットが不正です");
        	            }
        	    	}
        	    	else {
        	    		System.out.println("支店定義ファイルのフォーマットが不正です");
        	    		break;

        	    	}
        	}
            System.out.println(BranchMap);
		    br.close();
    	}
    	catch(IOException e) {
    		System.out.println(e);
    		System.out.println("支店定義ファイルが存在しません");
    	}


    	HashMap<String,String> ProductMap = new HashMap<String ,String>();
    	//商品定義ファイルの格納
    	try{
		    File file = new File(directory ,"commodity.lst");
		    FileReader fr = new FileReader(file);
		    BufferedReader br = new BufferedReader(fr);
            String a;
            String[] temp;

            while((a = br.readLine()) != null) {
        	    	temp = a.split(",");
        	        //商品定義フォーマットの判別式
        	    	if(temp[0].length() == 8) {
        	            ProductMap.put(temp[0],temp[1]);
        	    	}
        	    	else {
        	    		System.out.println("商品定義ファイルのフォーマットが不正です");
        	    	}
        	}
            System.out.println(ProductMap);
		    br.close();
    	}
    	catch(IOException e) {
    		System.out.println(e);
    		System.out.println("商品定義ファイルが存在しません");
    	}


    	//売り上げファイルの読み込み
		ArrayList <String>rcdList = new ArrayList<String>();
		ArrayList<Map.Entry<String,Integer>> BranchSaleList = new ArrayList<Map.Entry<String,Integer>>(BranchSaleSum.entrySet());

		try {
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
			    else {
			    }
			}
			System.out.println(rcdList);

			//rcdListの連番チェック


			//rcdファイルをひとつずつ処理する
            int i ; // i + 1番目のrcdファイルを処理中
			String[] saleTemp;
			saleTemp = new String[3]; //[0]=支店コード[1]=商品コード[2]=金額
            for(i = 0; i < rcdList.size(); i++) {
            	File file = new File(directory ,rcdList.get(i));
    	    	FileReader fr = new FileReader(file);
    			BufferedReader br = new BufferedReader(fr);

    			//ファイルの読み込み、ここでファイルの中身が四桁以上あるときのエラーをやる
    			//try-catchがネストしているのも問題
    			String a;
    			int count = 0;
    			while((a = br.readLine()) != null){
    				try{
    					saleTemp[count] = a;
    				    count += 1;
    				}
    				catch(ArrayIndexOutOfBoundsException e) {
    					System.out.println(e);
    					System.out.println( "のフォーマットが不正です");
    				}
    			}

    			//支店合計
    			BranchSaleSum.put(saleTemp[0] , BranchSaleSum.get(saleTemp[0]) + Integer.parseInt(saleTemp[2]));
    			//10桁を超えたらループを抜けて読み込み中断
                if (BranchSaleSum.get(saleTemp[0]) > 1000000000) {
                	System.out.println("合計金額が十桁を超えました");
                	break;
                }

    			br.close();
            }
            //支店の出力（確認用）
			for(String key : BranchSaleSum.keySet()) {
				System.out.println(key + "," + BranchMap.get(key) +"," +BranchSaleSum.get(key) );

			}
			//ソート
            Collections.sort(BranchSaleList, new Comparator<Map.Entry<String,Integer>>(){

            	@Override
            	public int compare(
            		Entry<String,Integer> entry1, Entry<String,Integer> entry2 ) {
            		return ((Integer)entry2.getValue()).compareTo((Integer)entry1.getValue());
            	}
            });

            //支店コード、支店名、合計額の出力（確認用）
            System.out.println(BranchSaleList);
            for (Entry<String,Integer> entry : BranchSaleList) {
            	System.out.println(entry.getKey() + "," + BranchMap.get(entry.getKey()) + "," + entry.getValue());
            }

        }
		catch(IOException e) {
			System.out.println(e);
			System.out.println("売り上げファイルが存在しません");
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
		      for (Entry<String,Integer> entry : BranchSaleList) {
	            	pw.println(entry.getKey() + "," + BranchMap.get(entry.getKey()) + "," + entry.getValue());
	            }

	          //FileWriterオブジェクトをクローズ
	          pw.close();
		    }
		    catch(IOException e){
		      System.out.println(e);
		    }

	}
}
