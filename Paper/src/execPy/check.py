# -*- coding:gb2312 -*-

import zipfile
from lxml import etree
import re
import sys
import shutil
import time
import os
import tempfile
import chardet

Rule_DirPath = sys.argv[1]#输入文件夹的路径,命令行的第二个参数
Data_DirPath = sys.argv[2]#输出文件夹的路径，命令行的第三个参数

Rule_Filename = Rule_DirPath + 'rules.txt'
Docx_Filename = Data_DirPath + 'origin.docx'

# Docx_Filename= raw_input("please input the path of your docx:")
# Rule_Filename='rules.txt'

word_schema='{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
Unicode_bt ='gb2312' #中文字符编码方式，我的机器上是gb2312，服务器上是utf-8，还有个别机器上是GBK

#以下4个函数是关于解压缩word文档和读取xml内容并按节点标签对树形结构进行迭代
def get_word_xml(docx_filename):
    zipF = zipfile.ZipFile(docx_filename)
    xml_content = zipF.read('word/document.xml')
    style_content=zipF.read('word/styles.xml')
    return xml_content,style_content

def get_xml_tree(xml_string):
    return etree.fromstring(xml_string)

def _iter(my_tree,type_char):
    for node in my_tree.iter(tag=etree.Element):
        if _check_element_is(node,type_char):
            yield node

def _check_element_is(element,type_char):
    return element.tag == '%s%s' % (word_schema,type_char)
 
#获取节点包含的文本内容
def get_ptext(w_p):
    ptext = ''
#-modified by zqd 20160125-----------------------
    for node in w_p.iter(tag=etree.Element):
        if _check_element_is(node,'t'):
#-----------------------------------------------
            ptext += node.text
    return ptext.encode(Unicode_bt,'ignore') #被检测的论文中可能出现奇怪的根本无法解码字符比如该同学从其他地方乱粘贴东西，因此加上个ignore参数忽略非法字符

#得到标题的等级
def get_level(w_p):
    for pPr in w_p:
        if _check_element_is(pPr,'pPr'):
            for pPr_node in pPr:
                if _check_element_is(pPr_node,'outlineLvl'):
                    return pPr_node.get('%s%s' %(word_schema,'val'))
                
                if _check_element_is(pPr_node,'pStyle'):
                    style_xml = etree.fromstring(zipfile.ZipFile(Docx_Filename).read('word/styles.xml'))
                    styleID = pPr_node.get('%s%s' %(word_schema,'val'))
                    flag = 1
                    while flag == 1 :
                        #print 'style',styleID
                        flag = 0
                        for style in _iter(style_xml,'style'):
                            if style.get('%s%s' %(word_schema,'styleId')) == styleID:
                                for style_node in style:
                                    if _check_element_is(style_node,'pPr'):
                                        for pPr_node in style_node:
                                            if _check_element_is(pPr_node,'outlineLvl'):
                                                return pPr_node.get('%s%s' %(word_schema,'val'))
                                    if _check_element_is(style_node,'basedOn'):
                                        styleID = style_node.get('%s%s' %(word_schema,'val'))
                                        flag = 1               

#-----------------------------    
#以下5个函数用来 获取文本对应的格式信息
#初始化一个格式字典，字段值的类型为方便处理均为str，值的定义和范围可以参考文档
def init_fd(d):
    d['fontCN']='宋体'
    d['fontEN']='Times New Roman'
    d['fontSize']='21'#因为word里默认是21
    d['paraAlign']='both'
    d['fontShape']='0'
    d['paraSpace']='240'
    d['paraIsIntent']='0'
    d['paraIsIntent1']='0'
    d['paraFrontSpace']='0'
    d['paraAfterSpace']='0'
    d['paraGrade']='0'
    return d

def has_key(node,attribute):
    return '%s%s' %(word_schema,attribute) in node.keys()

def get_val(node,attribute):
    if has_key(node,attribute):
        return node.get('%s%s' %(word_schema,attribute))
    else:
        return '未获取属性值'

#获取的格式信息赋给当前节点的格式字典
def assign_fd(node,d):
    for detail in node.iter(tag=etree.Element):
#------20160314 zqd----------------------------------
        if _check_element_is(detail,'rFonts'):
            if has_key(detail,'eastAsia'):#有此属性
                d['fontCN'] = get_val(detail,'eastAsia').encode(Unicode_bt)
            if has_key(detail,'ascii'):
                d['fontEN'] = get_val(detail,'ascii').encode(Unicode_bt)
#--------------------------------------------
        
        elif _check_element_is(detail,'sz'):
            d['fontSize'] = get_val(detail,'val')

        elif _check_element_is(detail,'jc'):
            d['paraAlign'] = get_val(detail,'val')

        elif _check_element_is(detail,'b'):
            if has_key(detail,'val'):
                if get_val(detail, 'val') != '0' and get_val(detail, 'val') != 'false':
                    d['fontShape'] = '1'#表示bold
                else:
                    #print 'not blod'
                    d['fontShape'] = '0'
            else:
                d['fontShape'] = '1'#表示bold

        elif _check_element_is(detail,'spacing'):
            if has_key(detail,'line'):
                d['paraSpace'] = get_val(detail,'line')
            if has_key(detail,'before'):
                d['paraFrontSpace']= get_val(detail,'before')
            if has_key(detail,'after'):
                d['paraAfterSpace']= get_val(detail,'after')
#--------20160313 zqd----------------------------------------
        elif _check_element_is(detail,'ind'):
            #悬挂缩进生成错误信息
            if has_key(detail,'left') or has_key(detail,'hanging'):
                pass
            if has_key(detail,'firstLine'):
                d['paraIsIntent']=get_val(detail,'firstLine')
            if has_key(detail,'firstLineChars'):
                d['paraIsIntent1']=get_val(detail,'firstLineChars')
                #print d['paraIsIntent']
                #在这里两种缩进是不同的，具体看xml文档，firstLineChars优先级高
#-------------------------------------------------
        elif _check_element_is(detail,'outlineLvl'):
            d['paraGrade'] = get_val(detail,'val')
    return d

def get_default_styleId():
    for style in _iter(style_tree,'style'):
        if get_val(style,"type") == "paragraph":
            for name in _iter(style,"name"):
                if get_val(name,"val")=="Normal":
                    return get_val(style,"styleId")

#----20160314 zqd------------
def get_style_format(styleID,d):
    for style in _iter(style_tree,'style'):
        if get_val(style,'styleId') == styleID:#get_val是属性
            for detail in style.iter(tag=etree.Element):
                if _check_element_is(detail,'basedOn'):
                    styleID1 = get_val(detail,'val')
                    get_style_format(styleID1,d)
                if _check_element_is(detail,'pPr'):
                    assign_fd(detail,d)

def get_style_rpr(styleID,d):
    for style in _iter(style_tree,'style'):
        if get_val(style,'styleId') == styleID:#get_val是属性
            for detail in style.iter(tag=etree.Element):
                if _check_element_is(detail,'basedOn'):
                    styleID1 = get_val(detail,'val')
                    get_style_rpr(styleID1,d)
                if _check_element_is(detail,"rPr"):
                    assign_fd(detail,d)
            
#获取格式
def get_format(node,d):
    init_fd(d)
    defaultId = get_default_styleId()
    get_style_format(defaultId,d)
    #关于我的2013版的word的pPr下的rPr不起作用
    for pPr in _iter(node,'pPr'):
        for pstyle in _iter(pPr,'pStyle'):
            styleID = get_val(pstyle,'val')
            get_style_format(styleID,d)
        assign_fd(pPr,d)
    d['fontCN']='宋体'
    d['fontEN']='Times New Roman'
    d['fontSize']='21'
    d['fontShape']='0'
    get_style_rpr(defaultId,d)
    for pPr in _iter(node,'pPr'):
        for pstyle in _iter(pPr,'pStyle'):
            styleID = get_val(pstyle,'val')
            get_style_rpr(styleID,d)
    return d

#--------------------------------------------------
def first_locate():
    paraNum = 0
    part[1] = 'cover'
    reference = []
    current_part = ''
    for paragr in _iter(xml_tree,'p'):
        paraNum +=1
        text=get_ptext(paragr)
        if not text or text == '' or text == '':
            continue
        for r in paragr.iter(tag=etree.Element):
            if _check_element_is(r, 'r'):
                for instr in r.iter(tag=etree.Element):
                    if _check_element_is(instr, 'instrText'):
                        refer = False
                        if "REF _Ref" in instr.text:
                            refer = True
                        if refer is True:
                            reference.append((instr.text[8:].split())[0])
        if '本人声明' in text:
            current_part = part[paraNum] = 'statement'
        elif '北京航空航天大学' in text  or '本科生毕业设计（论文）任务书' in text:
            current_part = part[paraNum] = 'taskbook'
        elif '论文封面书脊' in text:
            current_part = part[paraNum] = 'spine'
        elif re.compile(r'摘 *要').match(text):
            current_part = part[paraNum] = 'abstract'
        elif 'Abstract' in text or 'abstract' in text or 'ABSTRACT' in text:
            current_part = part[paraNum] = 'abstract_en'
        elif re.compile(r'目 *录').match(text) or re.compile(r'图 *目 *录').match(text) or re.compile(r'表 *目 *录').match(text) or re.compile(r'图 *表 *目 *录').match(text):
            current_part = part[paraNum] = 'menu'
        elif (current_part == 'menu' and not text[-1].isdigit()) or( re.compile(r'.*绪 *论').match(text) and not text[-1].isdigit()):
            current_part = part[paraNum] = 'body'
        elif text == '参考文献':
            current_part = part[paraNum] = 'refer'
        elif text.startswith('附录'):
            current_part = part[paraNum] = 'appendix'
    if not 'statement' in part.values():
        print 'warning statement doesnot exsit'
    if not 'spine' in part.values():
        print 'warning spine'
    if not 'abstract' in part.values():
        print 'warning abstract'
    if not 'body' in part.values():
        print 'warning body'
    if not 'menu' in part.values():
        print 'warning menu'
    return reference

def second_locate():
    paraNum = 0
    locate[1] = 'cover1'
    cur_part = ''
    cur_state = 'cover1'
    title = ''
    warnInfo=[]
    last_text = ''
    mentioned = []
    ref_num = 0
    for paragr in _iter(xml_tree,'p'):
        paraNum +=1
        text=get_ptext(paragr)
        if text == '' or text == '':
            continue
        if paraNum in part.keys():
            cur_part = part[paraNum]
        if cur_part == 'body':
            #------hsy add object detection July.13.2016--
            for node in paragr.iter(tag = etree.Element):
                if _check_element_is(node,'r'):
                    for innode in node.iter(tag = etree.Element):
                        if _check_element_is(innode,'object') or _check_element_is(innode,'drawing'):
                            cur_state = locate[paraNum] = 'object'
                            break
                if _check_element_is(node,'bookmarkStart'):
                    if node.values()[1][:4] == '_Ref':
                        if node.values()[1][4:] in reference:
                                mentioned.append(node.values()[1][4:])
            #------end
        if cur_part == 'cover':
            if '毕业设计'in text:
                cur_state = locate[paraNum] = 'cover2'
            elif  cur_state == 'cover2':
                cur_state = locate[paraNum] = 'cover3'
                title = text
            elif '院'in text and'系'in text and '名称'in text:
                cur_state = locate[paraNum] = 'cover4'
            elif '年'in text and '月'in text:
                cur_state = locate[paraNum] = 'cover5'
        elif cur_part == 'spine':
            if '论文封面书脊' in text:
                cur_state = locate[paraNum] = 'cover6'
            else:
                cur_state = locate[paraNum] = 'spine1'#不用处理了
        elif cur_part == "taskbook":
            cur_state = locate[paraNum] = "taskbook"
        elif cur_part == 'statement':
            if text == '本人声明':
                cur_state = locate[paraNum] = 'statm1'
            elif text.startswith('我声明'):
                cur_state = locate[paraNum] = 'statm2'
            elif '作者'in text:
                cur_state = locate[paraNum] = 'statm3'
            elif '时间' in text and  '年'in text  and '月' in text:
                last_text = text
            elif '时间'in last_text and  '年' in last_text and '月' in last_text and title in text:
                last_text = ''
                cur_state = locate[paraNum] = 'abstr1'
                
            elif '学'and'生'in text:
                cur_state = locate[paraNum] = 'abstr2'
        elif cur_part == 'abstract':
            if re.match(r'摘 *要',text):
                cur_state = locate[paraNum] = 'abstr3'
                last_text = text
            elif re.match(r'摘 *要',last_text):
                last_text = ''
                cur_state = locate[paraNum] = 'abstr4'
            elif '关键词'in text or '关键字'in text:
                cur_state = locate[paraNum] = 'abstr5'
                last_text = text
            elif cur_state == 'abstr5':
                last_text = ''
                cur_state = locate[paraNum] = 'abstr1'
            elif 'Author' in text:
                cur_state = locate[paraNum] = 'abstr2'
        elif cur_part == 'abstract_en':
            if text == 'ABSTRACT' or text == "Abstract" or text == 'abstract':
                cur_state = locate[paraNum] = 'abstr3'
                last_text = text
            elif (last_text == 'ABSTRACT' or last_text == 'Abstract' or text=="abstract")\
                 and 'Author'not in text and 'Tutor'not in text:
                cur_state = locate[paraNum] = 'abstr4'
                last_text = ''
            elif (('KEY'in text or 'key' in text or "Key" in text) and ('WORD'in text or'word' in text))\
                 or ('keyword'in text or 'Keyword'in text or'KEYWORD'in text): 
                cur_state = locate[paraNum] = 'abstr5'
        elif cur_part == 'menu':
            if re.match(r'目 *录',text)or re.compile(r'图 *目 *录').match(text) or re.compile(r'表 *目 *录').match(text) or re.compile(r'图 *表 *目 *录').match(text):
                cur_state = locate[paraNum] = 'menuTitle'
            elif analyse(text)  in ['firstLv','firstLv_e']:
                cur_state = locate[paraNum] ='menuFirst'
            elif analyse(text) in ['secondLv',"secondLv_e","secondLv_e2"]:
                cur_state = locate[paraNum] = 'menuSecond'
            elif analyse(text) in ['thirdLv',"thirdLv_e"]:
                cur_state = locate[paraNum] = 'menuThird'
            else :
                cur_state = locate[paraNum] ='menuFirst'#以汉字开头的标题都认为是一级标题
            if locate[paraNum] != 'menuTitle' and not text[-1].isdigit():
                cur_state = part[paraNum] = 'body'
                cur_part = 'body'
        elif cur_part == 'body':
            #得到级别，先按级别走，如果级别为普通，则按正则走。
            #print paraNum
            level = get_level(paragr)
            analyse_result = analyse(text)
            if analyse_result in['firstLv_e','secondLv_e','thirdLv_e']:
                warnInfo.append('    warning: 标题标号需要和标题之间用空格隔开')
                spaceNeeded.append(paraNum)
        #-------follow----hsy--modifies on July.13.2016
            if analyse_result is 'objectT':
                if cur_state != 'object':
                    #print 'warning',text
                    warnInfo.append('   warning: 图标题前没有直接对应的图')
            if cur_state is 'object':
                if analyse_result != 'objectT':
                    #print 'warning',text
                    warnInfo.append('   warning: 图后没有对应的图注。')
        #------end---------------------
            if level == '0':
                cur_state = locate[paraNum] = 'firstTitle'
                if analyse_result != 'firstLv' or analyse_result != 'firstLv_e':
                    #print 'warning',text
                    warnInfo.append('    warning: 标题级别和标题标号代表的级别不一致')
            elif level == '1':
                cur_state = locate[paraNum] = 'secondTitle'
                if analyse_result != 'secondLv' or analyse_result != 'secondLv_e':
                    #print 'warning',text
                    warnInfo.append('    warning: 标题级别和标题标号代表的级别不一致')
            elif level == '2':
                cur_state = locate[paraNum] = 'thirdTitle'
                if analyse_result != 'thirdLv' or analyse_result != 'thirdLv_e':
                    #print 'warning',text
                    warnInfo.append('    warning: 标题级别和标题标号代表的级别不一致')
            else:
                if paragr.getparent().tag != '%s%s'% (word_schema,'body'): #当paragr的父节点不是body时，该para的文本不属于正文（可能是表格、图形或文本框内的文字
                    cur_state = locate[paraNum] = 'tableText'
                elif analyse_result == 'firstLv':
                    cur_state = locate[paraNum] = 'firstTitle'
                elif analyse_result == 'secondLv' or analyse_result == 'secondLv_e':
                    cur_state = locate[paraNum] = 'secondTitle'
                elif analyse_result == 'thirdLv'or analyse_result == 'thirdLv_e':
                    cur_state = locate[paraNum] = 'thirdTitle'
                elif analyse_result == 'objectT':
                    cur_state = locate[paraNum] = 'objectTitle'
                elif analyse_result == 'tableT':
                    cur_state = locate[paraNum] = 'tableTitle'
                elif re.match(r'结 *论',text):
                    cur_state = locate[paraNum] = 'firstTitle'
                elif re.match(r'致 *谢',text):
                    cur_state = locate[paraNum] = 'firstTitle'
                else:
                    cur_state = locate[paraNum] = 'body'
        elif cur_part == 'refer':
            if text == '参考文献':
                cur_state = locate[paraNum] = 'firstTitle'
            else :
                cur_state = locate[paraNum] = 'reference'
                #得到参考文献的字典 zwl新增
                ref_num += 1
                islist = 0
                listnumFmt = ''
                for node in paragr.iter(tag=etree.Element):
                    if _check_element_is(node,'ilvl') or _check_element_is(node,'numId'):
                        islist += 1
                ref_dic[ref_num] = {}
                ref_dic[ref_num]['bookmarkStart'] = None
                ref_dic[ref_num]['ilvl'] = None
                ref_dic[ref_num]['numId'] = None
                for node in _iter(paragr,'bookmarkStart'):
                    if has_key(node,'name'):
                        ref_dic[ref_num]['bookmarkStart'] = get_val(node,'name')
                        break
                if islist == 2:
                    for node in paragr.iter(tag=etree.Element):
                        if _check_element_is(node,'ilvl'):
                            ref_dic[ref_num]['ilvl'] = get_val(node,'val')
                        if _check_element_is(node,'numId'):
                            ref_dic[ref_num]['numId'] = get_val(node,'val')
                    listnumFmt = getformat(getabstractnumId(ref_dic[ref_num]['numId']),ref_dic[ref_num]['ilvl'])[2]
                if not re.match('\\[[0-9]+\\]',text) and listnumFmt != '[%1]':
                     warnInfo.append('    warning: 参考文献必须以[num]标号开头！')
        elif cur_part == 'appendix':
            if text.startswith('附') and text.endswith('录'):
                cur_state = locate[paraNum] = 'firstTitle'
            else:
                cur_state = locate[paraNum] = 'body'
    for val in mentioned:
        if val in reference:
            reference.remove(val)
    return warnInfo


#在判别文本以什么开头的方法上使用了正则表达式
def analyse(text):
    text=text.strip(' ')
    if text.isdigit():
        return 'body'
    pat1 = re.compile('[0-9]+')#以数字开头的正则表达式
    pat2 = re.compile('[0-9]+\\.[0-9]')#以X.X开头的正则表达式
    pat3 = re.compile('[0-9]+\\.[0-9]\\.[0-9]')#以X.X.X开头的正则表达式
    pat4 = re.compile('图(\s)*[0-9]+(\\.|-)[0-9]')#图标题的正则表达式
    pat5 = re.compile('表(\s)*[0-9]+(\\.|-)[0-9]')#表标题的正则表达式

    #20160107 zqd -----------------------------------------------------------------------
    if pat1.match(text) and len(text)<70:
        if  pat1.sub('',text)[0] == ' ':
            sort = 'firstLv'
            #print 'the first LV length is',len(text)
        elif  pat1.sub('',text)[0] =='.':
            if pat2.match(text):
                if pat2.sub('',text)[0] == ' ':
                    sort = 'secondLv'
                elif pat2.sub('',text)[0]=='.':
                    if pat3.match(text):
                        if pat3.sub('',text)[0]==' ':
                            sort = 'thirdLv'
                        elif pat3.sub('',text)[0]=='.':
                            sort = 'overflow'
                            #print '    warning: 不允许出现四级标题！'
                        else:
                            sort ='thirdLv_e'
                    else:
                        sort='secondLv_e2'
                        #print '    warning: 二级标题正确的标号格式为X.X！'
                else:
                    sort = 'secondLv_e'
            else:
                sort = 'body'
        else:
            sort = 'firstLv_e'
    elif pat4.match(text) and len(text)<125:
        sort = 'objectT'
    elif pat5.match(text) and len(text)<125:
        sort = 'tableT'
    else :
        sort ='body'
#  zqd--------------------------------------------------------------------------------
    return sort

#读取规则接口
def read_rules(filename):
    f = open(filename,'r')
    #该字典主要是由于与前端接口定义不一致为了避免大片修改代码而定义的(已更新一致
    keyNameDc={'封面_单位代码':'cover1',
               '封面_毕业设计':'cover2',
               '封面_论文标题':'cover3',
               '封面_个人信息':'cover4',
               '封面_日期':'cover5',
               '封面_书脊':'cover6',
               '个人声明_标题':'statm1',
               '个人声明_正文':'statm2',
               '个人声明_签名':'statm3',
               '摘要_论文题目':'abstr1',
               '摘要_学生及教师姓名':'abstr2',
               '摘要_标题':'abstr3',
               '摘要_内容':'abstr4',
               '摘要_关键词':'abstr5',
               '摘要_关键词内容':'abstr6',
               '目录_标题':'menuTitle',
               '目录_一级标题':'menuFirst',
               '目录_二级标题':'menuSecond',
               '目录_三级标题':'menuThird',
               '正文_一级标题':'firstTitle',
               '正文_二级标题':'secondTitle',
               '正文_三级标题':'thirdTitle',
               '正文_段落':'body',
               '致谢_标题':'unknown',#
               '致谢_内容':'unknown',#
               '附录_标题':'extentTitle',
               '附录_内容':'extentContent',
               '图标题':'objectTitle',
               '表标题':'tableTitle',
               '参考文献_条目':'reference'
               }
    
    rules_dct={}        
    for line in f:
        if line.startswith('{'):
            group=line[1:-3].split(',')
            for factor in group:
                _key = factor[:factor.index(':')]
                _val = factor[factor.index(':')+1:]
                if _key == 'key':
                    rule_dkey = _val
                    rules_dct.setdefault(_val,{})
                if _key!= 'key':
                    rules_dct[rule_dkey].setdefault(_key,_val)
    f.close()
    return rules_dct

#检查格式并输出结果
def check_out(rule,to_check,locate,paraNum,paragr):
    errorInfo = []
    #这个字典的定义主要是由于前台那个同学规则字段和错误类型字段的名称不一致，神烦
    errorTypeName={'fontCN':'font',
                   'fontEN':'font',
                   'fontSize':'fontsize',
                   'fontShape':'fontshape',
                   'paraAlign':'gradeAlign',
                   "paraGrade":"paraLevel",
                   'paraSpace':'gradeSpace',
                   'paraFrontSpace':'gradeFrontSpace',
                   'paraAfterSpace':'gradeAfterSpace',
                   'paraIsIntent':'FLind'
                   }
    errorTypeDescrip={'fontCN':'中文字体',
                   'fontEN':'英文字体',
                   'fontSize':'字号',
                   'fontShape':'字形',
                   'paraAlign':'对齐方式',
                   'paraGrade':"文本级别",
                   'paraSpace':'行间距',
                   'paraFrontSpace':'段前间距',
                   'paraAfterSpace':'段后间距',
                   'paraIsIntent':'首行缩进'
                      }

    position = ['fontCN','fontEN','fontSize','fontShape','paraGrade','paraAlign','paraSpace','paraFrontSpace','paraAfterSpace','paraIsIntent']
    #这个字典的定义是为了避免对每个para都把规则字典里十个字段检查一遍，根据para的位置有选择有针对性的检查
    checkItemDct={'cover1':['fontCN','fontEN','fontSize','fontShape'],
                  'cover2':['fontCN','fontSize','paraAlign'],
                  'cover3':['fontCN','fontSize','paraAlign'],
                  'cover4':['fontCN','fontSize','fontShape'],
                  'cover5':['fontCN','fontSize','fontShape','paraAlign'],
                  'cover6':['fontCN','fontSize','fontShape','paraAlign'],
                  'statm1':position,
                  'statm2':position,
                  'statm3':position,
                  'abstr1':position,
                  'abstr2':position,
                  'abstr3':position,
                  'abstr4':position,
                  'abstr5':position,
                  'abstr6':position,
                  'menuTitle':position,
                  'menuFirst':['fontCN','fontSize','fontShape'],
                  'menuSecond':['fontCN','fontSize','fontShape'],
                  'menuThird':['fontCN','fontSize','fontShape'],
                  'firstTitle':position,
                  'secondTitle':position,
                  'thirdTitle':position,
                  'body':position,
                  'tableText':position,
                  'thankTitle':position,
                  'thankContent':position,
                  'extentTitle':position,
                  'extentContent':position,
                  'objectTitle':['fontCN','fontEN','fontSize','fontShape','paraGrade','paraAlign','paraIsIntent'],
                  'tableTitle':['fontCN','fontEN','fontSize','fontShape','paraGrade','paraAlign','paraIsIntent'],
                  'reference':position}
    islist = 0
    for numPr in _iter(paragr,"numPr"):
        islist = 1
    if locate in checkItemDct.keys():
        #关键词这里比较特殊，要深入para内部分析run的rpr来看关键词内容的格式
        if location == 'abstr5': 
            if ':' not in ptext and '：' not in ptext:
                rp.write('warning: 关键词后面没有冒号！\n')
                comment_txt.write('warning: 关键词后面没有冒号\n')
            found = 0
            #关键词和关键词内容都需要加粗
            for rpr_keyword in _iter(paragr,'rPr'):
                for bold_sign in _iter(rpr_keyword,'b'):
                    found = 1
            if not found:
                rp.write('warning: 关键词内容没有加粗！\n')
                comment_txt.write("warning:关键词内容没有加粗\n")
        else:
            for key in checkItemDct[locate]:
                if key == 'paraIsIntent':#对于缩进，特别处理
                    if islist == 0:
                        #print '00000000000000000',to_check['paraIsIntent1'],to_check['paraIsIntent']
                        if to_check['paraIsIntent1'] != '未获取属性值' and to_check['paraIsIntent1'] != '0':
                            if to_check['paraIsIntent1'] != '200' and rule['paraIsIntent'] == '1':
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent1_200\n')
                                rp.write(to_check['paraIsIntent1']+"段落缩进有误1\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("段落缩进有误\n")
                            elif rule['paraIsIntent'] == '0':
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent1_0\n')
                                rp.write(to_check['paraIsIntent1']+"段落缩进有误2\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("段落缩进有\n")
                        else:
                            #if to_check['paraIsIntent'] != str(int(rule['paraIsIntent'])*int(rule[key])*20):
                            if int(to_check['paraIsIntent']) > 0 and rule['paraIsIntent'] is '0':#这里做一个粗略的设定，因为要是按照上面注释的一行来执行，错误率太高了
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent_'+str(20*int(to_check['fontSize'])*int(rule[key]))+'\n')
                                rp.write(to_check['paraIsIntent']+"段落缩进有误3\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("段落缩进有误\n")
                            elif int(to_check['paraIsIntent']) < 100 and rule[key] == '1':
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent_'+str(20*int(to_check['fontSize'])*int(rule[key]))+'\n')
                                rp.write(to_check['paraIsIntent']+"段落缩进有误4\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("段落缩进有误\n")
                        continue
                elif key == "fontSize" or key == "fontShape":
                    font_size = []
                    font_shape = []
                    for r in _iter(paragr,"r"):
                        rtext = ""
                        for t in _iter(r,"t"):
                            rtext += t.text
                        if rtext == "":
                            continue
                        elif key == "fontSize":
                            flag = 1
                            for sz in _iter(r,"sz"):
                                flag = 0
                                if get_val(sz,"val") not in font_size:
                                    font_size.append(get_val(sz,"val"))
                                break
                            if flag == 1:
                                if to_check[key] not in font_size:
                                    font_size.append(to_check[key])
                        elif key == "fontShape":
                            flag = 1
                            for b in _iter(r,"b"):
                                flag = 0
                                if has_key(b,'val'):
                                    if get_val(b, 'val') != '0' and get_val(b, 'val') != 'false' and '1' not in font_shape:
                                        font_shape.append('1')#表示bold
                                    elif '0' not in font_shape:
                                        font_shape.append('0')
                                elif '1' not in font_shape:
                                    font_shape.append('1')
                                break
                            if flag == 1:
                                if to_check[key] not in font_shape:
                                    font_shape.append(to_check[key])
                    if key == "fontSize":
                        if len(font_size) > 1 or font_size[0] != rule[key]:
                            rp.write('    '+errorTypeDescrip[key]+'是'+ str(font_size) + '  正确应为：'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'是'+ str(font_size) + '  正确应为：'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')
                    elif key == "fontShape":
                        if len(font_shape) > 1 or font_shape[0] != rule[key]:
                            rp.write('    '+errorTypeDescrip[key]+'是'+ str(font_shape) + '  正确应为：'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'是'+ str(font_shape) + '  正确应为：'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')
                elif key == "fontCN" or key == "fontEN":
                    font_EN = []
                    font_CN = []
                    for r in _iter(paragr,"r"):
                        rtext = ""
                        for t in _iter(r,"t"):
                            rtext += t.text
                        if rtext == "":
                            continue
                        elif key == "fontCN":
                            flag = 1
                            for rfonts in _iter(r,"rFonts"):
                                if has_key(rfonts,'eastAsia'):
                                    flag = 0
                                    if get_val(rfonts, 'eastAsia').encode(Unicode_bt) not in font_CN:
                                        font_CN.append(get_val(rfonts,"eastAsia").encode(Unicode_bt))
                                break
                            if flag == 1:
                                if to_check[key] not in font_CN:
                                    font_CN.append(to_check[key])
                        elif key == "fontEN":
                            flag = 1
                            for rfonts in _iter(r,"rFonts"):
                                if has_key(rfonts,"ascii"):
                                    flag = 0
                                    if get_val(rfonts,"ascii").encode(Unicode_bt) not in font_EN:
                                        font_EN.append(get_val(rfonts,"ascii").encode(Unicode_bt))
                                break
                            if flag == 1:
                                if to_check[key] not in font_EN:
                                    font_EN.append(to_check[key])
                    if key == "fontCN":
                        if len(font_CN) > 1 or font_CN[0] != rule[key]:
                            rp.write('    '+errorTypeDescrip[key]+'是')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'是')
                            for font in font_CN:
                                rp.write(font+" ")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write(font+" ")
                            rp.write('正确应为：'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write('正确应为：'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')
                    elif key == "fontEN":
                        if len(font_EN) > 1 or font_EN[0] != rule[key]:
                            rp.write('    '+errorTypeDescrip[key]+'是')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'是')
                            for font in font_EN:
                                rp.write(font+" ")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write(font+" ")
                            rp.write('正确应为：'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write('正确应为：'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')

                else:
                    if to_check[key] != rule[key]:
                        rp.write('    '+errorTypeDescrip[key]+'是'+to_check[key]+'  正确应为：'+rule[key]+'\n')
                        if location not in['menuFirst','menuSecond','menuThird']:
                            comment_txt.write(errorTypeDescrip[key]+'是'+ to_check[key] + '  正确应为：'+rule[key]+'\n')
                        errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                        rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')
    return errorInfo

def grade2num():
    # 20160121 zqd
    # modified: xml_tree
    numPrIlvl = [0,0,0,0]
    for paragr in _iter(xml_tree,'p'):
        for pPr in paragr.iter(tag=etree.Element):
            if _check_element_is(pPr,'pPr'):
                for numPr in pPr.iter(tag=etree.Element):
                    if _check_element_is(numPr,'numPr'):
                        for ilvl in numPr.iter(tag=etree.Element):
                            if _check_element_is(ilvl,'ilvl'):
                                if has_key(ilvl,'val'):#有此属性
                                    result = get_val(ilvl,'val')
                                    if result == "0":
                                        numPrIlvl[0] += 1
                                        numPrIlvl[1] = numPrIlvl[2] = numPrIlvl[3] = 0
                                    elif result == "1":
                                        numPrIlvl[1] += 1
                                        numPrIlvl[2] = numPrIlvl[3] = 0
                                    elif result == "2":
                                        numPrIlvl[2] += 1
                                        numPrIlvl[3] = 0
                                    elif result == "3":
                                        numPrIlvl[3] += 1
                                    strNumPr = ""
                                    i = 0
                                    while numPrIlvl[i] != 0 and i < 4:
                                        strNumPr += str(numPrIlvl[i])+"."
                                        i+=1
                                    pPr.remove(numPr)
                                    for node in paragr.iter(tag=etree.Element):
                                        if _check_element_is(node,'t'):
                                            node.text = strNumPr+" "+node.text
                                            break
                                    #print etree.tostring(paragr,encoding="UTF-8",pretty_print=True)

#判断是否中文
def is_chinese(uchar):
    """ 判断一个unicode是否是汉字 """
    if uchar >= u'\u4e00' and uchar<=u'\u9fa5 ':
        return True
    else :
        return  False

#判断段落中有无交叉引用 zwl
def contain_ref(para,paraNum):
    flag = 0
    refnum = 0
    ref_value = {}
    ref_value['ref'] = None
    pat = re.compile(' *REF *')
    ref_text = ''
    for r in _iter(para,'r'):
        fldflag = 0
        for fldchar in _iter(r,"fldChar"):
            fldflag = 1
            if has_key(fldchar,'fldCharType'):
                if get_val(fldchar,'fldCharType') == 'begin':
                    flag = 1
                elif get_val(fldchar,'fldCharType') == 'separate':
                    flag = 2
                elif get_val(fldchar,'fldCharType') == 'end':
                    if ref_value['ref'] != None and re.match('\\[[0-9]+\\]',ref_text):
                        ref_value['text'] = ref_text
                        existflag = 0
                        for i in ref_dic:
                            if ref_dic[i]['bookmarkStart'] == ref_value['ref']:
                                existflag = 1
                                if ref_text[1:-1] == str(i):
                                    break
                                else:
                                    #print("*************************交叉引用与目标不符，可能未更新,正确应为["+str(i)+']')
                                    rp1.write(str(paraNum) + '_' + str(refnum) + '_error_ref_['+str(i)+']'+'\n')
                                    rp.write("参考文献的交叉引用与目标不符,未更新\n")
                                    comment_txt.write("参考文献的交叉引用与参考文献列表中对应的列表项不符，可能未更新\n")
                        if existflag == 0:
                            #print("*************************交叉引用未在参考文献列表中找到对应的列表项，可能未更新")
                            #rp1.write(str(paraNum) + '_' + str(refnum) + '_error_ref_['+str(i)+']\n')
                            rp.write("参考文献的交叉引用未在参考文献列表中找到对应的列表项，可能未更新\n")
                            comment_txt.write("参考文献的交叉引用未在参考文献列表中找到对应的列表项，可能未更新\n")
                    ref_text = ''
                    flag = 0
                    ref_value['ref'] == None
        if flag == 1 and fldflag == 0:
            for instrText in _iter(r,'instrText'):
                if pat.match(instrText.text):
                    ref_value['ref'] = pat.sub("",instrText.text)[0:13]
                    refnum += 1
                    #print ref_value['ref']
        if flag == 2 and ref_value['ref']!= None and fldflag == 0:
            vertAlignValue = None
            for text in _iter(r,'t'):
                ref_text = ref_text + text.text
            if re.match('\\[[0-9]+\\]',ref_text):
                for vertAlign in _iter(r,'vertAlign'):
                    vertAlignValue = get_val(vertAlign,'val')
                if vertAlignValue != 'superscript':
                    rp.write("参考文献的交叉引用未使用上标\n")
                    comment_txt.write("参考文献的交叉引用未使用上标\n")
                    rp1.write(str(paraNum) + '_' + str(refnum) + '_error_refVertAlign_superscript\n')

def getabstractnumId(numid):
    for num in _iter(numbering_tree,'num'):
        numId = get_val(num,'numId')
        #print numId
        if numId == numid:
            for abstractnum in _iter(num,'abstractNumId'):
                abstractnumId = get_val(abstractnum,'val')
                return abstractnumId

def getformat(abstractnumid,ilvl):
    start = ''
    numFmt = ''
    lvlText = ''
    for abstractNum in _iter(numbering_tree,"abstractNum"):
        if get_val(abstractNum,'abstractNumId') == abstractnumid:
            for lvl in _iter(abstractNum,'lvl'):
                if get_val(lvl,'ilvl') == ilvl:
                    for node in lvl.iter(tag=etree.Element):
                        if _check_element_is(node,"start"):
                            start = get_val(node,'val')
                        if _check_element_is(node,'numFmt'):
                            numFmt = get_val(node,'val')
                        if _check_element_is(node,"lvlText"):
                            lvlText = get_val(node,'val')
                    return start,numFmt,lvlText

def graphOrExcelTitle(ObjectFlag,ptext,paraNum,paragr):
    #hsy
    graphTitlePattern = re.compile('图(\s)*[0-9]+(\\.|-)[0-9]')#图标题的正则表达式
    wrongGraphTitlePattern = re.compile('图(\s)*[0-9]')#错误图标题的正则表达式
    excelTitlePattern = re.compile('表(\s)*[0-9]+(\\.|-)[0-9]')#表标题的正则表达式
    wrongExcelTitlePattern = re.compile('表(\s)*[0-9]')#错误表标题的正则表达式
    #
    if ObjectFlag == 1:
        if not graphTitlePattern.match(ptext):
            rp.write('     warning: 找不到对应图注 ----->'+ptext+'\n')
            #print('     warning: 找不到对应图注 ----->'+ptext)
        ObjectFlag = 0
    if graphTitlePattern.match(ptext):
        if paraNum - 1 in locate.keys():
            if locate[paraNum - 1] != 'object':
                rp.write('    warning: 没有对应的图。--->' + ptext + '\n')
             #   print('    warning: 没有对应的图。--->' + ptext)
        else:
            rp.write('    warning: 没有对应的图。--->' + ptext + '\n')
            #print('    warning: 没有对应的图。--->' + ptext)
        found = False
        for node in paragr.iter(tag=etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag=etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    此图注没被引用过' + ptext + '\n')
            #print('    此图注没被引用过' + ptext + '\n')
    if wrongGraphTitlePattern.match(ptext) and not graphTitlePattern.match(ptext):
        rp.write('    warning: 请改为符合规则的图注 ------>' + ptext + '\n')
        #print('    warning: 请改为符合规则的图注 ------>' + ptext)
        found = False
        for node in paragr.iter(tag=etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag=etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    此图注没被引用过' + ptext + '\n')
         #   print('    此图注没被引用过' + ptext )
    if excelTitlePattern.match(ptext):
        found = False
        for node in paragr.iter(tag = etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag = etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    此图注没被引用过' + ptext + '\n')
          #  print('    此图注没被引用过' + ptext)
    if wrongExcelTitlePattern.match(ptext) and not excelTitlePattern.match(ptext):
        rp.write('    warning: 请改为符合规则的图注------->'+ptext+'\n')
        #print('    warning: 请改为符合规则的图注------->'+ptext+'\n')
        found = False
        for node in paragr.iter(tag=etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag=etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    warning: 此图注没被引用过' + ptext + '\n' )
         #   print('    warning: 此图注没被引用过' + ptext)
                
startTime=time.time()
#主程序__main__入口差不多在这里了
xml_from_file,style_from_file = get_word_xml(Docx_Filename)
xml_tree   = get_xml_tree(xml_from_file)
style_tree = get_xml_tree(style_from_file)
zipF = zipfile.ZipFile(Docx_Filename)
numbering_content = zipF.read("word/numbering.xml")
numbering_tree = etree.fromstring(numbering_content)
rules_dct=read_rules(Rule_Filename)

Part='start'
previousL='unknown'

part = {}
locate = {}
paraNum=0

#hsy
reference = []
spaceNeeded = []
ObjectFlag=0
#
empty_para=0
#参考文献字典zwl
ref_dic = {}
#
rp = open(Data_DirPath + 'check_out.txt','w')
rp1 = open(Data_DirPath + 'check_out1.txt','w')
rp2 = open(Data_DirPath + 'space.txt','w')
comment_txt = open(Data_DirPath + "comment.txt","w")
#sys.exit()
reference = first_locate()
warninglist = second_locate()

eInfo = ''
section_seq = 0
rp.write('''论文格式检查报告文档使用说明：
*****此版本为初次上线测试版，难免存在误判等许多问题，遇到误判时请谅解，并可以将问题反馈给我们完善程序∧_∧*****
各字段值说明：
位置  为程序判断该段落文本在你论文中可能的位置，如果发现与你的论文中位置不符，请忽略其后的格式检查信息
字形  0表示未加粗，1表示加粗
行间距 N=数值/240，即为N倍行距
段前段后的数值单位均为磅
首行缩进0表示首行未缩进，1表示首行缩进2字符
warning信息表示可能存在的问题，不一定准确
**********并不华丽的分割线（然并卵）**********
''')
p_format={}.fromkeys(['fontCN','fontEN','fontSize','paraAlign','fontShape','paraSpace',
                         'paraIsIntent','paraFrontSpace','paraAfterSpace','paraGrade'])
for paragr in _iter(xml_tree,'p'):
#以<w:p>为最小单位迭代
    paraNum +=1
    ptext=get_ptext(paragr)
    if paraNum in locate.keys():
        location = locate[paraNum]
    if location not in['menuFirst','menuSecond','menuThird']:
        comment_txt.write("Id:"+str(paraNum)+'\n')
    if ptext == ' ' or ptext == '':
        empty_para += 1
        warnInfo=[]
        if empty_para>=2:
            rp.write(' \n    warning:不允许出现连续空行 \n')
            if location not in['menuFirst','menuSecond','menuThird']:
                comment_txt.write("null\n")
                comment_txt.write("warning:不允许出现连续空行\n")
        else:
            if location not in['menuFirst','menuSecond','menuThird']:
                comment_txt.write('null\n')
                comment_txt.write("格式正确：第一个空行\n")
        continue
    if not is_chinese(ptext.decode(Unicode_bt)[0]) and not (ptext.decode(Unicode_bt)[0] >= '\0' and ptext.decode(Unicode_bt)[0] <= chr(127)):
        if location not in['menuFirst','menuSecond','menuThird']:
            comment_txt.write("notChineseOrAscii\n")
            comment_txt.write("格式正确：段落开头非中文与ascii字符；\n")
        continue
    empty_para =0
    if location not in['menuFirst','menuSecond','menuThird']:
        comment_txt.write(location+'\n')
    get_format(paragr,p_format)
    #下面这函数判断当前图表标注的引用，生成错误信息，还未写修改的方法
    graphOrExcelTitle(ObjectFlag,ptext,paraNum,paragr)
    if location == 'object':
        ObjectFlag = 1
        comment_txt.write("object\n")
        comment_txt.write("格式正确：段落为图片\n")
        continue
    rp.write(str(paraNum)+' '+ptext+' ' + location + '\n')
    first_text = 0
    if location != 'taskbook' and (ptext.startswith(" ") or ptext.startswith("　")):
        rp.write("    段首有空格\n")
        rp1.write(str(paraNum)+'_'+'paraStart'+'_error_'+'startWithSpace'+'_0\n')
    contain_ref(paragr,paraNum)                 
    
    if location in rules_dct.keys():
        rp.write('    位置：'+rules_dct[location]['name']+'\n')
        errorInfo = check_out(rules_dct[location],p_format,location,paraNum,paragr)
    else:
        errorInfo=''
    if errorInfo :
        pass
    else:
        rp.write('    检查： 格式正确\n')
        if location not in['menuFirst','menuSecond','menuThird']:
            comment_txt.write('检查：格式正确\n')

for num in spaceNeeded:
    rp2.write('%d' %num)
    rp2.write('\n') 

endTime=time.time()
print '   用时： %.2f ms' % (100*(endTime-startTime))


hyperlinks = []
bookmarks = []
#检查目录是否自动更新
for node in _iter(xml_tree, 'hyperlink'):
    temp=''
    for hl in _iter(node,'t'):
        temp += hl.text
    hyperlinks.append(node.values()[0])
for node in _iter(xml_tree, 'bookmarkStart'):
    bookmarks.append(node.values()[1])

catalog_ud= True
for i in hyperlinks:  
    if i not in bookmarks:
        catalog_ud =False
if catalog_ud:
    pass
else:
    pass

rp.write('\n\n\n论文格式检查完毕！\n')
rp.close()
rp1.close()
rp2.close()
comment_txt.close()
