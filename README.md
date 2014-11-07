strepsimur
==========

Scala wrappers for Galago (IR retrieval component) for the Strepsirrhini project


Part of the [strepsirrhini project](http://github.com/laura-dietz/strepsirrhini/)  

Maven dependency through Nexus
===============================
All sub-projects are available through our Nexus server

        <repository>
            <id>edu.umass.ciir.releases</id>
            <name>CIIR Nexus Releases</name>
            <url>http://scm-ciir.cs.umass.edu:8080/nexus/content/repositories/releases/</url>
        </repository>


Required Dependencies
=======================

    <properties>
        <scala.version>2.10.2</scala.version>
        <lemur.ware>org.lemurproject.galago</lemur.ware>
        <lemur.version>3.7-SNAPSHOT</lemur.version>
        <javaVersion>1.6</javaVersion>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <strepsi.version>1.5-SNAPSHOT</strepsi.version>
    </properties>


    <!-- This module -->
    <dependency>    
        groupId>edu.umass.ciir</groupId>
        <artifactId>strepsimur</artifactId>
        <version>s${scala.version}-g${lemur.version}-${strepsi.version}</version>
    </dependency>
    
    <!-- galago dependency -->    
    <dependency>
        <groupId>org.lemurproject.galago</groupId>
        <artifactId>core</artifactId>
        <version>${lemur.version}</version>
    </dependency>

    <!-- dependency on strepsitools from the strepsirrhini project -->
    <dependency>
        <groupId>edu.umass.ciir</groupId>
        <artifactId>strepsitools</artifactId>
        <version>s${scala.version}-${strepsi.version}</version>
    </dependency>
