/*
 * Copyright Balsamiq Studios, Inc.  All rights reserved.  http://www.balsamiq.com
 *
 */

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
    if (!name) {
      throwTagError("Tag [swf] is missing required attribute [dir]")
    }
    String base = attrs.remove('base')

    String link = g.resource(dir: dir, file: "${name}.swf", base: base)

    if (swfTagPostProcessor) {
       link = swfTagPostProcessor.process(link, request, false)
     }

     out << link
  }
}
