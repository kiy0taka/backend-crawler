#!./lib/runner.groovy
// Generates server-side metadata for Perl auto-installation
import com.gargoylesoftware.htmlunit.html.*;

import net.sf.json.*
import com.gargoylesoftware.htmlunit.WebClient

def wc = new WebClient()
wc.setThrowExceptionOnScriptError(false)
def baseUrl = 'http://www.cpan.org/src/5.0/'
HtmlPage p = wc.getPage('http://search.cpan.org/dist/perl/');

def json = [];

p.selectNodes('//select[@class="extend"]/option').each { e ->
    def ver = (e.getAttribute('value') =~ /^.*?\/perl-([^ ]+).*/)
    if (ver) {
        def url = baseUrl + "perl-" + ver[0][1] + ".tar.gz";
//        println url
        json << ["id":ver[0][1], "name": "Perl ${ver[0][1]}".toString(), "url":url];
    }
}

//json = json.sort{a,b -> new VersionNumber(a.id).compareTo(new VersionNumber(b.id)) }
json = json.sort{a,b -> a.id.compareTo(b.id) }

lib.DataWriter.write("org.jenkinsci.plugins.perlinstaller.PerlInstaller",JSONObject.fromObject([list:json]));
