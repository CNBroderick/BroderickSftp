## 测试代码

```Java
  @Test
  public void test25() {
      // 设置工作根目录
      File base = new File("E:\\Broderick Developments\\libs\\");

      // 生成build文件
      File build = AntBuildXmlGenerator.create().setBase(base).generateXml();
      
      // 调用Ant输出并返回控制台打印的字符串
      String result = AntExecutor.create().setBuildXml(build).execute()
      System.out.println(result);
  }
```

## 运行结果

    makeSuperJar:
          [jar] Building jar: E:\Broderick Developments\libs\merge.jar

    BUILD SUCCESSFUL
    Total time: 7 seconds


    Process finished with exit code 0

## MAVEN 依赖

```xml
  <dependency>
    <groupId>org.apache.ant</groupId>
    <artifactId>ant</artifactId>
    <version>1.10.3</version>
  </dependency>
```
