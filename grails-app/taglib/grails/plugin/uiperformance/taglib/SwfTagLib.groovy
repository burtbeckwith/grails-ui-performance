package grails.plugin.uiperformance.taglib

class SwfTagLib extends AbstractTaglib {
  static namespace = 'p'

  def swfTagPostProcessor

  def swf = { attrs, body ->
    String name = attrs.remove('name')
    if (!name) {
      throwTagError("Tag [swf] is missing required attribute [name]")
    }
    String dir = attrs.remove('dir')
    if (!dir) {
      throwTagError("Tag [swf] is missing required attribute [dir]")
    }
    String link = generateRelativePath(dir, name, '.swf',
        attrs.remove('plugin'), attrs.remove('absolute'), attrs.remove('base'))

    if (swfTagPostProcessor) {
       link = swfTagPostProcessor.process(link, request, false)
     }

     out << link
  }
}
