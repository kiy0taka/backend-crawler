#!/usr/bin/env groovy
// Generates server-side metadata for MongoDB auto-installation
@GrabResolver(name="repo.jenkins-ci.org",root='http://repo.jenkins-ci.org/public/')
@Grab(group="org.jvnet.hudson",module="htmlunit",version="2.2-hudson-9")
@Grab(group="org.jenkins-ci",module="update-center2",version="1.20")
import com.gargoylesoftware.htmlunit.html.*;
import net.sf.json.*
import com.gargoylesoftware.htmlunit.WebClient
import org.jvnet.hudson.update_center.Signer

def wc = new WebClient()
def json = [];
[osx: ['i386', 'x86_64'],
    linux: ['i686', 'x86_64'],
    win32: ['i386', 'x86_64'],
    sunos5: ['i86pc', 'x86_64']
].each { osname, archs -> archs.each { arch ->
    HtmlPage p = wc.getPage("http://dl.mongodb.org/dl/$osname/$arch")
    p.selectNodes("//a[@href]").reverse().collect { HtmlAnchor e ->
        def m = e.getHrefAttribute() =~ /^.*mongodb-$osname-$arch-(.*?)\.(tgz|zip)$/
        if (m) {
            String version = "${osname}-${arch}-${m[0][1]}"
            json << [id:version, name:version, url:m[0][0]]
        }
    }
}}

JSONObject envelope = JSONObject.fromObject([list:json])
new Signer().configureFromEnvironment().sign(envelope);
println envelope.toString(2)
key = "org.jenkinsci.plugins.mongodb.MongoDBInstaller"
File d = new File("target")
d.mkdirs()
new File(d,"${key}.json").write("downloadService.post('${key}',${envelope.toString(2)})")
