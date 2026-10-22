(function(){
var R=document.documentElement;
function get(){try{return localStorage.getItem('theme')}catch(e){return null}}
function set(v){try{localStorage.setItem('theme',v)}catch(e){}}
var t=get();if(t==='light'||t==='dark')R.setAttribute('data-theme',t);
document.addEventListener('DOMContentLoaded',function(){
var $=function(i){return document.getElementById(i)};
var b=$('theme');
if(b)b.addEventListener('click',function(){
var cur=R.getAttribute('data-theme')||(matchMedia('(prefers-color-scheme: light)').matches?'light':'dark');
var n=cur==='light'?'dark':'light';R.setAttribute('data-theme',n);set(n)});
var y=$('yr');if(y)y.textContent=new Date().getFullYear();
// Release info
function setBtn(a,href,text){if(!a)return;a.href=href;a.setAttribute('aria-disabled',href==='#install'||href==='#'?'true':'false');if(text)a.textContent=text}
fetch('/latest.json',{cache:'no-cache'}).then(function(r){if(!r.ok)throw 0;return r.json()}).then(function(d){
var apks=d.apks||{},url=apks['arm64-v8a']||apks.universal||apks[Object.keys(apks)[0]];
$('cl-version').textContent='MoMan '+d.version;
$('cl-date').textContent=d.releasedAt||'';
$('changelog-latest').textContent=d.changelog||'';
if(url){
$('dl-label').textContent='Download v'+d.version;
setBtn($('dl-btn'),url);setBtn($('dl-btn2'),url,'Download APK v'+d.version);
$('dl-meta').textContent='Version '+d.version+(d.releasedAt?' · '+d.releasedAt:'')+' · '+(apks['arm64-v8a']?'arm64-v8a':'universal');
$('dl-status').textContent='Latest version: '+d.version+'. '+(d.changelog||'');
var l=$('dl-links');l.textContent='';
Object.keys(apks).forEach(function(k){var a=document.createElement('a');a.href=apks[k];a.textContent=k;l.appendChild(a);l.appendChild(document.createTextNode(' '))});
}else{
$('dl-label').textContent='Coming soon';
$('dl-meta').textContent='Version '+d.version+' is being prepared. Check back shortly.';
$('dl-status').textContent='Coming soon: the first APK for version '+d.version+' has not been published yet.';
}
}).catch(function(){
$('cl-version').textContent='Unavailable';
$('dl-status').textContent='Could not load release info. Please try again later.';
$('dl-label').textContent='Coming soon';
});
});
})();
