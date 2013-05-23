package grails.plugin.uiperformance.postprocess

class SwfTagPostProcessor extends AbstractTagPostProcessor {
  boolean gzip = false

  @Override
  String process(String html, Object request, boolean quoted = true) {
    return super.process(html, request, quoted)
  }


  protected List getExtensions() {
    return ['swf']
  }
}
