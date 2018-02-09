package com.builtbroken.mc.core.deps;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * File dependency that can be downloaded from a meven location
 * Created by Dark on 7/29/2015.
 */
public class MavenDep extends Dep
{
    public final String repoURL;
    public final String groupID;
    public final String artifactID;
    public String classifier;
    public final String ext;

    public final Version version;

    public MavenDep(String mavenRepo, String groupId, String artifactId, String major, String minor, String revis, String build)
    {
        this(mavenRepo, groupId, artifactId, major, minor, revis, build, "");
    }

    public MavenDep(String mavenRepo, String groupId, String artifactId, String major, String minor, String revis, String build, String classifier)
    {
        this(mavenRepo, groupId, artifactId, Integer.parseInt(major), Integer.parseInt(minor), Integer.parseInt(revis), Integer.parseInt(build), classifier);
    }

    public MavenDep(String mavenRepo, String groupId, String artifactId, int major, int minor, int revis, int build)
    {
        this(mavenRepo, groupId, artifactId, new Version(major, minor, revis, build), "", ".jar");
    }

    public MavenDep(String mavenRepo, String groupId, String artifactId, int major, int minor, int revis, int build, String classifier)
    {
        this(mavenRepo, groupId, artifactId, new Version(major, minor, revis, build), classifier, ".jar");
    }

    public MavenDep(String mavenRepo, String groupId, String artifactId, int major, int minor, int revis, int build, String classifier, String ext)
    {
        this(mavenRepo, groupId, artifactId, new Version(major, minor, revis, build), classifier, ext);
    }

    public MavenDep(String mavenRepo, String groupId, String artifactId, Version version, String classifier, String ext)
    {
        this.repoURL = mavenRepo;
        this.groupID = groupId;
        this.artifactID = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.ext = ext;
    }

    public String getMavenFolderPath()
    {
        return this.groupID.replaceAll("\\.", "/") + "/" + this.artifactID + "/" + this.version();
    }

    public String version()
    {
        return version.toString();
    }

    @Override
    public String getFileName()
    {
        return this.artifactID + "-" + this.version() + (this.classifier.isEmpty() ? "" : "-" + this.classifier) + this.ext;
    }

    @Override
    public String getGenericFileName()
    {
        return artifactID;
    }

    @Override
    public boolean isNewerVersion(String fileName)
    {
        return this.version.isNewer(getVersion(fileName));
    }

    /**
     * Converts the file name into a version object
     *
     * @param fileName - name of the file, including extension
     * @return version
     */
    public Version getVersion(String fileName)
    {
        if (fileName == null || fileName.isEmpty())
        {
            throw new IllegalArgumentException("File name for getVersion(String filename) can not be null or empty");
        }
        int firstIndex = fileName.indexOf("-");
        int secondIndex = fileName.indexOf("-", firstIndex + 1);
        if (secondIndex < 0)
        {
            secondIndex = fileName.lastIndexOf(".");
        }
        else if (classifier == null || classifier.isEmpty())
        {
            classifier = fileName.substring(secondIndex + 1, fileName.lastIndexOf("."));
        }
        return new Version(fileName.substring(firstIndex + 1, secondIndex));
    }

    @Override
    public URL getURL()
    {
        try
        {
            return new URL(this.repoURL + getMavenFolderPath() + "/" + getFileName());
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
