using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using ICSharpCode.SharpZipLib.Zip;
using UnityEngine;
using System.Text;
using UnityEngine;
public class CompressHelper
{
    /// <summary>
    /// 压缩多个文件/文件夹
    /// </summary>
    /// <param name="sourceList">源文件/文件夹路径列表</param>
    /// <param name="zipFilePath">压缩文件路径</param>
    /// <param name="comment">注释信息</param>
    /// <param name="password">压缩密码</param>
    /// <param name="compressionLevel">压缩等级，范围从0到9，可选，默认为6</param>
    /// <returns></returns>
    public static bool CompressFile(string filePath, IEnumerable<string> sourceList, string zipFilePath,
         string comment = null, string password = null, int compressionLevel = 6)
    {
        bool result = false;
        try
        {
            //检测目标文件所属的文件夹是否存在，如果不存在则建立
            string zipFileDirectory = Path.GetDirectoryName(zipFilePath);
            if (!Directory.Exists(zipFileDirectory))
            {
                Directory.CreateDirectory(zipFileDirectory);
            }

            Dictionary<string, string> dictionaryList = new Dictionary<string, string>();
            string[] str = Directory.GetFiles(filePath);

            using (ZipOutputStream zipStream = new ZipOutputStream(File.Create(zipFilePath)))
            {
                zipStream.Password = password;//设置密码
                zipStream.SetComment(comment);//添加注释
                zipStream.SetLevel(compressionLevel);//设置压缩等级

                foreach (string key in str)//从字典取文件添加到压缩文件
                {
                    if (File.Exists(key))//判断是文件还是文件夹
                    {
                        FileInfo fileItem = new FileInfo(key);

                        using (FileStream readStream = fileItem.Open(FileMode.Open,
                            FileAccess.Read, FileShare.Read))
                        {
                            ZipEntry zipEntry = new ZipEntry(key);
                            zipEntry.DateTime = fileItem.LastWriteTime;
                            zipEntry.Size = readStream.Length;
                            zipStream.PutNextEntry(zipEntry);
                            int readLength = 0;
                            byte[] buffer = new byte[2048];

                            do
                            {
                                readLength = readStream.Read(buffer, 0, 2048);
                                zipStream.Write(buffer, 0, readLength);
                            } while (readLength == 2048);
                            readStream.Close();
                            Debug.Log(key + "压缩完毕");
                        }
                    }
                    else//对文件夹的处理
                    {
                        ZipEntry zipEntry = new ZipEntry(dictionaryList[key] + "/");
                        zipStream.PutNextEntry(zipEntry);
                    }
                }

                zipStream.Flush();
                zipStream.Finish();
                zipStream.Close();
            }

            result = true;
        }
        catch (System.Exception ex)
        {
            throw new Exception("压缩文件失败", ex);
        }

        return result;
    }

    /// <summary>
    /// 解压文件到指定文件夹
    /// </summary>
    /// <param name="sourceFile">压缩文件</param>
    /// <param name="destinationDirectory">目标文件夹，如果为空则解压到当前文件夹下</param>
    /// <param name="password">密码</param>
    /// <returns></returns>
    public static bool DecomparessFile(string sourceFile, string destinationDirectory = null, string password = null)
    {
        bool result = false;

        if (!File.Exists(sourceFile))
        {
            throw new FileNotFoundException("要解压的文件不存在", sourceFile);
        }

        if (string.IsNullOrWhiteSpace(destinationDirectory))
        {
            destinationDirectory = Path.GetDirectoryName(sourceFile);
        }

        try
        {
            if (!Directory.Exists(destinationDirectory))
            {
                Directory.CreateDirectory(destinationDirectory);
            }



            Encoding gbk = Encoding.GetEncoding("utf-8");
            //ZipConstants.DefaultCodePage = gbk.CodePage;
            
            using (ZipInputStream zipStream = new ZipInputStream(File.Open(sourceFile, FileMode.Open,
                FileAccess.Read, FileShare.Read)))
            {
                zipStream.Password = password;
                ZipEntry zipEntry = zipStream.GetNextEntry();

                while (zipEntry != null)
                {
                    if (zipEntry.IsDirectory)//如果是文件夹则创建
                    {
                        Directory.CreateDirectory(Path.Combine(destinationDirectory,
                            Path.GetDirectoryName(zipEntry.Name)));
                    }
                    else
                    {
                        string fileName = Path.GetFileName(zipEntry.Name);
                        if (!string.IsNullOrEmpty(fileName) && fileName.Trim().Length > 0)
                        {

                          

                            string ss = destinationDirectory + "/" + zipEntry.Name;


                            FileInfo fileItem = new FileInfo(ss);

                            zipEntry.Name.Split('/').Last();
                            using (FileStream writeStream = fileItem.Create())
                            {
                                byte[] buffer = new byte[2048];
                                int readLength = 0;

                                do
                                {
                                    readLength = zipStream.Read(buffer, 0, 2048);
                                    writeStream.Write(buffer, 0, readLength);
                                } while (readLength == 2048);

                                writeStream.Flush();
                                writeStream.Close();
                            }
                            fileItem.LastWriteTime = zipEntry.DateTime;
                        }
                    }

                    zipEntry = zipStream.GetNextEntry();//获取下一个文件
                }

                zipStream.Close();
            }
            result = true;
        }
        catch (System.Exception ex)
        {
        }

        return result;
    }
}

