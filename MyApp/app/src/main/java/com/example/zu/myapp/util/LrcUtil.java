package com.example.zu.myapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zu on 2016/4/18.
 */
/*
* 单例模式，调用getLrc(File) 传入歌词文件的File
* */
public class LrcUtil
{
    //单例模式
    static private LrcUtil lrcUtil;
    //最后完成的LRC，按照时间升序排列
    private TreeMap<Integer,String> lrc;
    private ArrayList<String> mList;
    //需要读取的文件
    private File lrcFile;

    private LrcUtil()
    {

    }

    public static LrcUtil getInstance()
    {
        if(lrcUtil==null)
        {
            lrcUtil=new LrcUtil();

        }
        return lrcUtil;
    }

    public TreeMap<Integer, String> getLrc(File lrcFile) {
        this.lrcFile=lrcFile;
        lrc=new TreeMap<>();
        loadFromFile(lrcFile);
        return lrc;
    }


    //将LRC数据从文件中读取出来并且转换
    private void loadFromFile(File file)
    {
        BufferedReader in;


        try
        {
            InputStreamReader reader=new InputStreamReader(new FileInputStream(file),"utf-8");
            in=new BufferedReader(reader);
            //StringBuilder sb=new StringBuilder();
            String s;
            mList=new ArrayList<>();
            while((s= in.readLine())!=null)
            {
                //sb.append(s+"\n");
                mList.add(s);
            }
            in.close();
            //System.out.print(sb.toString());


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

		/*
		*
		* */
        if(mList!=null) {
            for (int i = 0; i < mList.size(); i++) {
                TreeMap<Integer, String> ok;

                //匹配标题
                if ((ok = matchTitle(mList.get(i))) != null) {
                    System.out.println(ok.toString());
                    lrc.putAll(ok);
                    continue;
                }
                //匹配艺术家
                if ((ok = matchArtist(mList.get(i))) != null) {
                    System.out.println(ok.toString());
                    lrc.putAll(ok);
                    continue;
                }
                //匹配专辑
                if ((ok = matchAlbum(mList.get(i))) != null) {
                    System.out.println(ok.toString());
                    lrc.putAll(ok);
                    continue;
                }
                //匹配歌词作者
                if ((ok = matchAuthor(mList.get(i))) != null) {
                    System.out.println(ok.toString());
                    lrc.putAll(ok);
                    continue;
                }
                //匹配歌词内容
                if ((ok = matchContent(mList.get(i))) != null) {
                    System.out.println(ok.toString());
                    lrc.putAll(ok);
                    continue;
                }

            }
        }


    }

    private TreeMap<Integer,String> matchTitle(String s)
    {
        //System.out.println(s);

        //歌名格式为[ti:name]
        String reg=".*\\[ti:(.*)\\].*";
        Pattern pattern=Pattern.compile(reg);
        Matcher matcher=pattern.matcher(s);
        if(matcher.matches())
        {
            TreeMap<Integer,String>temp= new TreeMap<>();
            temp.put(-4,matcher.group(1));
            return temp;
        }
        return null;
    }

    //匹配艺术家，艺术家格式为[ar:name]
    private TreeMap<Integer,String> matchArtist(String s)
    {
        String reg=".*\\[ar:(.*)\\].*";
        Pattern pattern=Pattern.compile(reg);
        Matcher matcher=pattern.matcher(s);
        if(matcher.matches())
        {
            if(matcher.matches())
            {
                TreeMap<Integer,String>temp= new TreeMap<>();
                temp.put(-3,matcher.group(1));
                return temp;
            }
        }
        return null;
    }

    //匹配专辑，专辑格式为[al:name]
    private TreeMap<Integer,String> matchAlbum(String s)
    {
        String reg=".*\\[al:(.*)\\].*";
        Pattern pattern=Pattern.compile(reg);
        Matcher matcher=pattern.matcher(s);
        if(matcher.matches())
        {
            TreeMap<Integer,String>temp= new TreeMap<>();
            temp.put(-2,matcher.group(1));
            return temp;
        }
        return null;
    }

    //匹配歌词作者，格式为[by:name]
    private TreeMap<Integer,String> matchAuthor(String s)
    {
        String reg=".*\\[by:(.*)\\].*";
        Pattern pattern=Pattern.compile(reg);
        Matcher matcher=pattern.matcher(s);
        if(matcher.matches())
        {
            TreeMap<Integer,String>temp= new TreeMap<>();
            temp.put(-1,matcher.group(1));
            return temp;
        }
        return null;
    }

    /*
    * 匹配歌词内容，格式为[dd:dd.dd]content，或者[dd:dd:dd]content，并且，其中一行歌词可能有多个时间标签。
    * 所以匹配内容的时候先把所有的时间标签和内容分离出来，然后再依次把时间标签分离出来并且和内容一起放到map里
    * */
    private TreeMap<Integer,String> matchContent(String s)
    {
        //先提取所有的时间标签和内容
        String reg1="(\\[.*\\])(.*)";
        //(\[[0-9]{2}:[0-9]{2}[.|:][0-9]{2}\])(.*)
        Pattern pattern1=Pattern.compile(reg1);
        Matcher matcher1=pattern1.matcher(s);
        if(matcher1.matches())
        {
            //System.out.println("group count:"+matcher1.groupCount());
            TreeMap<Integer,String>temp= new TreeMap<>();
            /*
            * 然后针对时间标签进行分离，这里匹配出来的group(1)是第一个时间标签，
            * group(2)是剩下的所有时间标签，然后对剩下的进行分离
            * */

            String reg2="(\\[[0-9]{2}:[0-9]{2}[.|:][0-9]{2}\\])(.*)";
            String timeStamps=matcher1.group(1);
            while(true) {
                Pattern pattern2= Pattern.compile(reg2);
                Matcher matcher2=pattern2.matcher(timeStamps);
                if (matcher2.matches())
                {
                    Integer time=convertTime(matcher2.group(1));
                    temp.put(time,matcher1.group(matcher1.groupCount()));

                    //如果count==2，证明还有未分离完的标签
                    if(matcher2.groupCount()==2)
                    {
                        timeStamps=matcher2.group(2);
                    }
                    else
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }


            }
            return temp;
        }
        return null;
    }

    //将标签转换为时间，单位是10ms
    private Integer convertTime(String s)
    {
        //时间标签格式为[dd:dd.dd]或[dd:dd:dd]
        String reg="\\[([0-9]{2}):([0-9]{2})[.|:]([0-9]{2})\\]";
        Pattern pattern=Pattern.compile(reg);
        Matcher matcher=pattern.matcher(s);
        if(matcher.matches())
        {
            return Integer.parseInt(matcher.group(1))*60*100+Integer.parseInt(matcher.group(2))*100+Integer.parseInt(matcher.group(3));
        }
        return null;
    }



}
