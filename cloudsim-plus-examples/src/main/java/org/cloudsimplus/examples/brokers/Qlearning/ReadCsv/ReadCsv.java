package org.cloudsimplus.examples.brokers.Qlearning.ReadCsv;

import java.util.ArrayList;

import com.csvreader.CsvReader;
/**
 * java读取目录下所有csv文件数据，存入二维数组并返回
 * 用 JavaCSV的API 说明文档：http://javacsv.sourceforge.net/
 * JavaCSV官网：https://sourceforge.net/projects/javacsv/
 * @param  ：totalCloudlets 指定列的全部负载时间序列
 * @args ：
 */

public class ReadCsv {
    private static String filePath = "C:\\Users\\qjncn\\Desktop\\data_new1.csv";
    public static ArrayList<Integer> totalCloudlets=new ArrayList<Integer> ();

    public static void main(String[] args) {
        //读取0数据中心的负载，利用构造函数
        ReadCsv readCsv=new ReadCsv(0);
        readCsv.getJob(0);

    }

    public ReadCsv(int column){
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 不读表头，注释掉
            //csvReader.readHeaders();
            while (csvReader.readRecord()) {
                // 读取一行原始数据
                //System.out.println(csvReader.getRawRecord());
                //csvReader.getRawRecord();
                // 读该行的某一列,
                System.out.println(csvReader.get(column));
                int temp = -1;
                try {
                    temp = Integer.valueOf(csvReader.get(column)).intValue();//将str类型的抓换成integer类
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                totalCloudlets.add(temp);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 按照模拟时间执行总负载的提取
     * 涉及ArrayList的方法get(),和Integer的方法intValue()
     * @param timeIndex  时间点，需要配合sim核心模拟
     * @return 返回当前时刻总的负载数
     */
    public int getJob(int timeIndex){
        int tmp = totalCloudlets.get(timeIndex).intValue();
        System.out.println(tmp );
        return tmp;
    }
}
