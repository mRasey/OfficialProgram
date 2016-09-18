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

Rule_DirPath = sys.argv[1]#�����ļ��е�·��,�����еĵڶ�������
Data_DirPath = sys.argv[2]#����ļ��е�·���������еĵ���������

Rule_Filename = Rule_DirPath + 'rules.txt'
Docx_Filename = Data_DirPath + 'origin.docx'

# Docx_Filename= raw_input("please input the path of your docx:")
# Rule_Filename='rules.txt'

word_schema='{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
Unicode_bt ='gb2312' #�����ַ����뷽ʽ���ҵĻ�������gb2312������������utf-8�����и����������GBK

#����4�������ǹ��ڽ�ѹ��word�ĵ��Ͷ�ȡxml���ݲ����ڵ��ǩ�����νṹ���е���
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
 
#��ȡ�ڵ�������ı�����
def get_ptext(w_p):
    ptext = ''
#-modified by zqd 20160125-----------------------
    for node in w_p.iter(tag=etree.Element):
        if _check_element_is(node,'t'):
#-----------------------------------------------
            ptext += node.text
    return ptext.encode(Unicode_bt,'ignore') #�����������п��ܳ�����ֵĸ����޷������ַ������ͬѧ�������ط���ճ����������˼��ϸ�ignore�������ԷǷ��ַ�

#�õ�����ĵȼ�
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
#����5���������� ��ȡ�ı���Ӧ�ĸ�ʽ��Ϣ
#��ʼ��һ����ʽ�ֵ䣬�ֶ�ֵ������Ϊ���㴦���Ϊstr��ֵ�Ķ���ͷ�Χ���Բο��ĵ�
def init_fd(d):
    d['fontCN']='����'
    d['fontEN']='Times New Roman'
    d['fontSize']='21'#��Ϊword��Ĭ����21
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
        return 'δ��ȡ����ֵ'

#��ȡ�ĸ�ʽ��Ϣ������ǰ�ڵ�ĸ�ʽ�ֵ�
def assign_fd(node,d):
    for detail in node.iter(tag=etree.Element):
#------20160314 zqd----------------------------------
        if _check_element_is(detail,'rFonts'):
            if has_key(detail,'eastAsia'):#�д�����
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
                    d['fontShape'] = '1'#��ʾbold
                else:
                    #print 'not blod'
                    d['fontShape'] = '0'
            else:
                d['fontShape'] = '1'#��ʾbold

        elif _check_element_is(detail,'spacing'):
            if has_key(detail,'line'):
                d['paraSpace'] = get_val(detail,'line')
            if has_key(detail,'before'):
                d['paraFrontSpace']= get_val(detail,'before')
            if has_key(detail,'after'):
                d['paraAfterSpace']= get_val(detail,'after')
#--------20160313 zqd----------------------------------------
        elif _check_element_is(detail,'ind'):
            #�����������ɴ�����Ϣ
            if has_key(detail,'left') or has_key(detail,'hanging'):
                pass
            if has_key(detail,'firstLine'):
                d['paraIsIntent']=get_val(detail,'firstLine')
            if has_key(detail,'firstLineChars'):
                d['paraIsIntent1']=get_val(detail,'firstLineChars')
                #print d['paraIsIntent']
                #���������������ǲ�ͬ�ģ����忴xml�ĵ���firstLineChars���ȼ���
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
        if get_val(style,'styleId') == styleID:#get_val������
            for detail in style.iter(tag=etree.Element):
                if _check_element_is(detail,'basedOn'):
                    styleID1 = get_val(detail,'val')
                    get_style_format(styleID1,d)
                if _check_element_is(detail,'pPr'):
                    assign_fd(detail,d)

def get_style_rpr(styleID,d):
    for style in _iter(style_tree,'style'):
        if get_val(style,'styleId') == styleID:#get_val������
            for detail in style.iter(tag=etree.Element):
                if _check_element_is(detail,'basedOn'):
                    styleID1 = get_val(detail,'val')
                    get_style_rpr(styleID1,d)
                if _check_element_is(detail,"rPr"):
                    assign_fd(detail,d)
            
#��ȡ��ʽ
def get_format(node,d):
    init_fd(d)
    defaultId = get_default_styleId()
    get_style_format(defaultId,d)
    #�����ҵ�2013���word��pPr�µ�rPr��������
    for pPr in _iter(node,'pPr'):
        for pstyle in _iter(pPr,'pStyle'):
            styleID = get_val(pstyle,'val')
            get_style_format(styleID,d)
        assign_fd(pPr,d)
    d['fontCN']='����'
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
        if '��������' in text:
            current_part = part[paraNum] = 'statement'
        elif '�������պ����ѧ' in text  or '��������ҵ��ƣ����ģ�������' in text:
            current_part = part[paraNum] = 'taskbook'
        elif '���ķ����鼹' in text:
            current_part = part[paraNum] = 'spine'
        elif re.compile(r'ժ *Ҫ').match(text):
            current_part = part[paraNum] = 'abstract'
        elif 'Abstract' in text or 'abstract' in text or 'ABSTRACT' in text:
            current_part = part[paraNum] = 'abstract_en'
        elif re.compile(r'Ŀ *¼').match(text) or re.compile(r'ͼ *Ŀ *¼').match(text) or re.compile(r'�� *Ŀ *¼').match(text) or re.compile(r'ͼ *�� *Ŀ *¼').match(text):
            current_part = part[paraNum] = 'menu'
        elif (current_part == 'menu' and not text[-1].isdigit()) or( re.compile(r'.*�� *��').match(text) and not text[-1].isdigit()):
            current_part = part[paraNum] = 'body'
        elif text == '�ο�����':
            current_part = part[paraNum] = 'refer'
        elif text.startswith('��¼'):
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
            if '��ҵ���'in text:
                cur_state = locate[paraNum] = 'cover2'
            elif  cur_state == 'cover2':
                cur_state = locate[paraNum] = 'cover3'
                title = text
            elif 'Ժ'in text and'ϵ'in text and '����'in text:
                cur_state = locate[paraNum] = 'cover4'
            elif '��'in text and '��'in text:
                cur_state = locate[paraNum] = 'cover5'
        elif cur_part == 'spine':
            if '���ķ����鼹' in text:
                cur_state = locate[paraNum] = 'cover6'
            else:
                cur_state = locate[paraNum] = 'spine1'#���ô�����
        elif cur_part == "taskbook":
            cur_state = locate[paraNum] = "taskbook"
        elif cur_part == 'statement':
            if text == '��������':
                cur_state = locate[paraNum] = 'statm1'
            elif text.startswith('������'):
                cur_state = locate[paraNum] = 'statm2'
            elif '����'in text:
                cur_state = locate[paraNum] = 'statm3'
            elif 'ʱ��' in text and  '��'in text  and '��' in text:
                last_text = text
            elif 'ʱ��'in last_text and  '��' in last_text and '��' in last_text and title in text:
                last_text = ''
                cur_state = locate[paraNum] = 'abstr1'
                
            elif 'ѧ'and'��'in text:
                cur_state = locate[paraNum] = 'abstr2'
        elif cur_part == 'abstract':
            if re.match(r'ժ *Ҫ',text):
                cur_state = locate[paraNum] = 'abstr3'
                last_text = text
            elif re.match(r'ժ *Ҫ',last_text):
                last_text = ''
                cur_state = locate[paraNum] = 'abstr4'
            elif '�ؼ���'in text or '�ؼ���'in text:
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
            if re.match(r'Ŀ *¼',text)or re.compile(r'ͼ *Ŀ *¼').match(text) or re.compile(r'�� *Ŀ *¼').match(text) or re.compile(r'ͼ *�� *Ŀ *¼').match(text):
                cur_state = locate[paraNum] = 'menuTitle'
            elif analyse(text)  in ['firstLv','firstLv_e']:
                cur_state = locate[paraNum] ='menuFirst'
            elif analyse(text) in ['secondLv',"secondLv_e","secondLv_e2"]:
                cur_state = locate[paraNum] = 'menuSecond'
            elif analyse(text) in ['thirdLv',"thirdLv_e"]:
                cur_state = locate[paraNum] = 'menuThird'
            else :
                cur_state = locate[paraNum] ='menuFirst'#�Ժ��ֿ�ͷ�ı��ⶼ��Ϊ��һ������
            if locate[paraNum] != 'menuTitle' and not text[-1].isdigit():
                cur_state = part[paraNum] = 'body'
                cur_part = 'body'
        elif cur_part == 'body':
            #�õ������Ȱ������ߣ��������Ϊ��ͨ���������ߡ�
            #print paraNum
            level = get_level(paragr)
            analyse_result = analyse(text)
            if analyse_result in['firstLv_e','secondLv_e','thirdLv_e']:
                warnInfo.append('    warning: ��������Ҫ�ͱ���֮���ÿո����')
                spaceNeeded.append(paraNum)
        #-------follow----hsy--modifies on July.13.2016
            if analyse_result is 'objectT':
                if cur_state != 'object':
                    #print 'warning',text
                    warnInfo.append('   warning: ͼ����ǰû��ֱ�Ӷ�Ӧ��ͼ')
            if cur_state is 'object':
                if analyse_result != 'objectT':
                    #print 'warning',text
                    warnInfo.append('   warning: ͼ��û�ж�Ӧ��ͼע��')
        #------end---------------------
            if level == '0':
                cur_state = locate[paraNum] = 'firstTitle'
                if analyse_result != 'firstLv' or analyse_result != 'firstLv_e':
                    #print 'warning',text
                    warnInfo.append('    warning: ���⼶��ͱ����Ŵ���ļ���һ��')
            elif level == '1':
                cur_state = locate[paraNum] = 'secondTitle'
                if analyse_result != 'secondLv' or analyse_result != 'secondLv_e':
                    #print 'warning',text
                    warnInfo.append('    warning: ���⼶��ͱ����Ŵ���ļ���һ��')
            elif level == '2':
                cur_state = locate[paraNum] = 'thirdTitle'
                if analyse_result != 'thirdLv' or analyse_result != 'thirdLv_e':
                    #print 'warning',text
                    warnInfo.append('    warning: ���⼶��ͱ����Ŵ���ļ���һ��')
            else:
                if paragr.getparent().tag != '%s%s'% (word_schema,'body'): #��paragr�ĸ��ڵ㲻��bodyʱ����para���ı����������ģ������Ǳ��ͼ�λ��ı����ڵ�����
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
                elif re.match(r'�� *��',text):
                    cur_state = locate[paraNum] = 'firstTitle'
                elif re.match(r'�� *л',text):
                    cur_state = locate[paraNum] = 'firstTitle'
                else:
                    cur_state = locate[paraNum] = 'body'
        elif cur_part == 'refer':
            if text == '�ο�����':
                cur_state = locate[paraNum] = 'firstTitle'
            else :
                cur_state = locate[paraNum] = 'reference'
                #�õ��ο����׵��ֵ� zwl����
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
                     warnInfo.append('    warning: �ο����ױ�����[num]��ſ�ͷ��')
        elif cur_part == 'appendix':
            if text.startswith('��') and text.endswith('¼'):
                cur_state = locate[paraNum] = 'firstTitle'
            else:
                cur_state = locate[paraNum] = 'body'
    for val in mentioned:
        if val in reference:
            reference.remove(val)
    return warnInfo


#���б��ı���ʲô��ͷ�ķ�����ʹ����������ʽ
def analyse(text):
    text=text.strip(' ')
    if text.isdigit():
        return 'body'
    pat1 = re.compile('[0-9]+')#�����ֿ�ͷ��������ʽ
    pat2 = re.compile('[0-9]+\\.[0-9]')#��X.X��ͷ��������ʽ
    pat3 = re.compile('[0-9]+\\.[0-9]\\.[0-9]')#��X.X.X��ͷ��������ʽ
    pat4 = re.compile('ͼ(\s)*[0-9]+(\\.|-)[0-9]')#ͼ�����������ʽ
    pat5 = re.compile('��(\s)*[0-9]+(\\.|-)[0-9]')#������������ʽ

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
                            #print '    warning: ����������ļ����⣡'
                        else:
                            sort ='thirdLv_e'
                    else:
                        sort='secondLv_e2'
                        #print '    warning: ����������ȷ�ı�Ÿ�ʽΪX.X��'
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

#��ȡ����ӿ�
def read_rules(filename):
    f = open(filename,'r')
    #���ֵ���Ҫ��������ǰ�˽ӿڶ��岻һ��Ϊ�˱����Ƭ�޸Ĵ���������(�Ѹ���һ��
    keyNameDc={'����_��λ����':'cover1',
               '����_��ҵ���':'cover2',
               '����_���ı���':'cover3',
               '����_������Ϣ':'cover4',
               '����_����':'cover5',
               '����_�鼹':'cover6',
               '��������_����':'statm1',
               '��������_����':'statm2',
               '��������_ǩ��':'statm3',
               'ժҪ_������Ŀ':'abstr1',
               'ժҪ_ѧ������ʦ����':'abstr2',
               'ժҪ_����':'abstr3',
               'ժҪ_����':'abstr4',
               'ժҪ_�ؼ���':'abstr5',
               'ժҪ_�ؼ�������':'abstr6',
               'Ŀ¼_����':'menuTitle',
               'Ŀ¼_һ������':'menuFirst',
               'Ŀ¼_��������':'menuSecond',
               'Ŀ¼_��������':'menuThird',
               '����_һ������':'firstTitle',
               '����_��������':'secondTitle',
               '����_��������':'thirdTitle',
               '����_����':'body',
               '��л_����':'unknown',#
               '��л_����':'unknown',#
               '��¼_����':'extentTitle',
               '��¼_����':'extentContent',
               'ͼ����':'objectTitle',
               '�����':'tableTitle',
               '�ο�����_��Ŀ':'reference'
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

#����ʽ��������
def check_out(rule,to_check,locate,paraNum,paragr):
    errorInfo = []
    #����ֵ�Ķ�����Ҫ������ǰ̨�Ǹ�ͬѧ�����ֶκʹ��������ֶε����Ʋ�һ�£���
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
    errorTypeDescrip={'fontCN':'��������',
                   'fontEN':'Ӣ������',
                   'fontSize':'�ֺ�',
                   'fontShape':'����',
                   'paraAlign':'���뷽ʽ',
                   'paraGrade':"�ı�����",
                   'paraSpace':'�м��',
                   'paraFrontSpace':'��ǰ���',
                   'paraAfterSpace':'�κ���',
                   'paraIsIntent':'��������'
                      }

    position = ['fontCN','fontEN','fontSize','fontShape','paraGrade','paraAlign','paraSpace','paraFrontSpace','paraAfterSpace','paraIsIntent']
    #����ֵ�Ķ�����Ϊ�˱����ÿ��para���ѹ����ֵ���ʮ���ֶμ��һ�飬����para��λ����ѡ��������Եļ��
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
        #�ؼ�������Ƚ����⣬Ҫ����para�ڲ�����run��rpr�����ؼ������ݵĸ�ʽ
        if location == 'abstr5': 
            if ':' not in ptext and '��' not in ptext:
                rp.write('warning: �ؼ��ʺ���û��ð�ţ�\n')
                comment_txt.write('warning: �ؼ��ʺ���û��ð��\n')
            found = 0
            #�ؼ��ʺ͹ؼ������ݶ���Ҫ�Ӵ�
            for rpr_keyword in _iter(paragr,'rPr'):
                for bold_sign in _iter(rpr_keyword,'b'):
                    found = 1
            if not found:
                rp.write('warning: �ؼ�������û�мӴ֣�\n')
                comment_txt.write("warning:�ؼ�������û�мӴ�\n")
        else:
            for key in checkItemDct[locate]:
                if key == 'paraIsIntent':#�����������ر���
                    if islist == 0:
                        #print '00000000000000000',to_check['paraIsIntent1'],to_check['paraIsIntent']
                        if to_check['paraIsIntent1'] != 'δ��ȡ����ֵ' and to_check['paraIsIntent1'] != '0':
                            if to_check['paraIsIntent1'] != '200' and rule['paraIsIntent'] == '1':
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent1_200\n')
                                rp.write(to_check['paraIsIntent1']+"������������1\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("������������\n")
                            elif rule['paraIsIntent'] == '0':
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent1_0\n')
                                rp.write(to_check['paraIsIntent1']+"������������2\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("����������\n")
                        else:
                            #if to_check['paraIsIntent'] != str(int(rule['paraIsIntent'])*int(rule[key])*20):
                            if int(to_check['paraIsIntent']) > 0 and rule['paraIsIntent'] is '0':#������һ�����Ե��趨����ΪҪ�ǰ�������ע�͵�һ����ִ�У�������̫����
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent_'+str(20*int(to_check['fontSize'])*int(rule[key]))+'\n')
                                rp.write(to_check['paraIsIntent']+"������������3\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("������������\n")
                            elif int(to_check['paraIsIntent']) < 100 and rule[key] == '1':
                                rp1.write(str(paraNum)+'_'+locate+'_'+'error_paraIsIntent_'+str(20*int(to_check['fontSize'])*int(rule[key]))+'\n')
                                rp.write(to_check['paraIsIntent']+"������������4\n")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write("������������\n")
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
                                        font_shape.append('1')#��ʾbold
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
                            rp.write('    '+errorTypeDescrip[key]+'��'+ str(font_size) + '  ��ȷӦΪ��'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'��'+ str(font_size) + '  ��ȷӦΪ��'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')
                    elif key == "fontShape":
                        if len(font_shape) > 1 or font_shape[0] != rule[key]:
                            rp.write('    '+errorTypeDescrip[key]+'��'+ str(font_shape) + '  ��ȷӦΪ��'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'��'+ str(font_shape) + '  ��ȷӦΪ��'+rule[key]+'\n')
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
                            rp.write('    '+errorTypeDescrip[key]+'��')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'��')
                            for font in font_CN:
                                rp.write(font+" ")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write(font+" ")
                            rp.write('��ȷӦΪ��'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write('��ȷӦΪ��'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')
                    elif key == "fontEN":
                        if len(font_EN) > 1 or font_EN[0] != rule[key]:
                            rp.write('    '+errorTypeDescrip[key]+'��')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write(errorTypeDescrip[key]+'��')
                            for font in font_EN:
                                rp.write(font+" ")
                                if location not in['menuFirst','menuSecond','menuThird']:
                                    comment_txt.write(font+" ")
                            rp.write('��ȷӦΪ��'+rule[key]+'\n')
                            if location not in['menuFirst','menuSecond','menuThird']:
                                comment_txt.write('��ȷӦΪ��'+rule[key]+'\n')
                            errorInfo.append('\'type\':\''+errorTypeName[key]+'\',\'correct\':\''+rule[key]+'\'')
                            rp1.write(str(paraNum)+'_'+locate+'_error_'+ key+'_'+ rule[key]+'\n')

                else:
                    if to_check[key] != rule[key]:
                        rp.write('    '+errorTypeDescrip[key]+'��'+to_check[key]+'  ��ȷӦΪ��'+rule[key]+'\n')
                        if location not in['menuFirst','menuSecond','menuThird']:
                            comment_txt.write(errorTypeDescrip[key]+'��'+ to_check[key] + '  ��ȷӦΪ��'+rule[key]+'\n')
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
                                if has_key(ilvl,'val'):#�д�����
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

#�ж��Ƿ�����
def is_chinese(uchar):
    """ �ж�һ��unicode�Ƿ��Ǻ��� """
    if uchar >= u'\u4e00' and uchar<=u'\u9fa5 ':
        return True
    else :
        return  False

#�ж϶��������޽������� zwl
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
                                    #print("*************************����������Ŀ�겻��������δ����,��ȷӦΪ["+str(i)+']')
                                    rp1.write(str(paraNum) + '_' + str(refnum) + '_error_ref_['+str(i)+']'+'\n')
                                    rp.write("�ο����׵Ľ���������Ŀ�겻��,δ����\n")
                                    comment_txt.write("�ο����׵Ľ���������ο������б��ж�Ӧ���б����������δ����\n")
                        if existflag == 0:
                            #print("*************************��������δ�ڲο������б����ҵ���Ӧ���б������δ����")
                            #rp1.write(str(paraNum) + '_' + str(refnum) + '_error_ref_['+str(i)+']\n')
                            rp.write("�ο����׵Ľ�������δ�ڲο������б����ҵ���Ӧ���б������δ����\n")
                            comment_txt.write("�ο����׵Ľ�������δ�ڲο������б����ҵ���Ӧ���б������δ����\n")
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
                    rp.write("�ο����׵Ľ�������δʹ���ϱ�\n")
                    comment_txt.write("�ο����׵Ľ�������δʹ���ϱ�\n")
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
    graphTitlePattern = re.compile('ͼ(\s)*[0-9]+(\\.|-)[0-9]')#ͼ�����������ʽ
    wrongGraphTitlePattern = re.compile('ͼ(\s)*[0-9]')#����ͼ�����������ʽ
    excelTitlePattern = re.compile('��(\s)*[0-9]+(\\.|-)[0-9]')#������������ʽ
    wrongExcelTitlePattern = re.compile('��(\s)*[0-9]')#���������������ʽ
    #
    if ObjectFlag == 1:
        if not graphTitlePattern.match(ptext):
            rp.write('     warning: �Ҳ�����Ӧͼע ----->'+ptext+'\n')
            #print('     warning: �Ҳ�����Ӧͼע ----->'+ptext)
        ObjectFlag = 0
    if graphTitlePattern.match(ptext):
        if paraNum - 1 in locate.keys():
            if locate[paraNum - 1] != 'object':
                rp.write('    warning: û�ж�Ӧ��ͼ��--->' + ptext + '\n')
             #   print('    warning: û�ж�Ӧ��ͼ��--->' + ptext)
        else:
            rp.write('    warning: û�ж�Ӧ��ͼ��--->' + ptext + '\n')
            #print('    warning: û�ж�Ӧ��ͼ��--->' + ptext)
        found = False
        for node in paragr.iter(tag=etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag=etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    ��ͼעû�����ù�' + ptext + '\n')
            #print('    ��ͼעû�����ù�' + ptext + '\n')
    if wrongGraphTitlePattern.match(ptext) and not graphTitlePattern.match(ptext):
        rp.write('    warning: ���Ϊ���Ϲ����ͼע ------>' + ptext + '\n')
        #print('    warning: ���Ϊ���Ϲ����ͼע ------>' + ptext)
        found = False
        for node in paragr.iter(tag=etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag=etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    ��ͼעû�����ù�' + ptext + '\n')
         #   print('    ��ͼעû�����ù�' + ptext )
    if excelTitlePattern.match(ptext):
        found = False
        for node in paragr.iter(tag = etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag = etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    ��ͼעû�����ù�' + ptext + '\n')
          #  print('    ��ͼעû�����ù�' + ptext)
    if wrongExcelTitlePattern.match(ptext) and not excelTitlePattern.match(ptext):
        rp.write('    warning: ���Ϊ���Ϲ����ͼע------->'+ptext+'\n')
        #print('    warning: ���Ϊ���Ϲ����ͼע------->'+ptext+'\n')
        found = False
        for node in paragr.iter(tag=etree.Element):
            if _check_element_is(node, 'r'):
                for bookmarks in node.iter(tag=etree.Element):
                    if _check_element_is(bookmarks, 'bookmarkStart'):
                        if bookmarks.values()[1][:4] == '_Ref':
                            found = True
        if not found:
            rp.write('    warning: ��ͼעû�����ù�' + ptext + '\n' )
         #   print('    warning: ��ͼעû�����ù�' + ptext)
                
startTime=time.time()
#������__main__��ڲ����������
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
#�ο������ֵ�zwl
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
rp.write('''���ĸ�ʽ��鱨���ĵ�ʹ��˵����
*****�˰汾Ϊ�������߲��԰棬����������е�������⣬��������ʱ���½⣬�����Խ����ⷴ�����������Ƴ����_��*****
���ֶ�ֵ˵����
λ��  Ϊ�����жϸö����ı����������п��ܵ�λ�ã�������������������λ�ò�������������ĸ�ʽ�����Ϣ
����  0��ʾδ�Ӵ֣�1��ʾ�Ӵ�
�м�� N=��ֵ/240����ΪN���о�
��ǰ�κ����ֵ��λ��Ϊ��
��������0��ʾ����δ������1��ʾ��������2�ַ�
warning��Ϣ��ʾ���ܴ��ڵ����⣬��һ��׼ȷ
**********���������ķָ��ߣ�Ȼ���ѣ�**********
''')
p_format={}.fromkeys(['fontCN','fontEN','fontSize','paraAlign','fontShape','paraSpace',
                         'paraIsIntent','paraFrontSpace','paraAfterSpace','paraGrade'])
for paragr in _iter(xml_tree,'p'):
#��<w:p>Ϊ��С��λ����
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
            rp.write(' \n    warning:����������������� \n')
            if location not in['menuFirst','menuSecond','menuThird']:
                comment_txt.write("null\n")
                comment_txt.write("warning:�����������������\n")
        else:
            if location not in['menuFirst','menuSecond','menuThird']:
                comment_txt.write('null\n')
                comment_txt.write("��ʽ��ȷ����һ������\n")
        continue
    if not is_chinese(ptext.decode(Unicode_bt)[0]) and not (ptext.decode(Unicode_bt)[0] >= '\0' and ptext.decode(Unicode_bt)[0] <= chr(127)):
        if location not in['menuFirst','menuSecond','menuThird']:
            comment_txt.write("notChineseOrAscii\n")
            comment_txt.write("��ʽ��ȷ�����俪ͷ��������ascii�ַ���\n")
        continue
    empty_para =0
    if location not in['menuFirst','menuSecond','menuThird']:
        comment_txt.write(location+'\n')
    get_format(paragr,p_format)
    #�����⺯���жϵ�ǰͼ���ע�����ã����ɴ�����Ϣ����δд�޸ĵķ���
    graphOrExcelTitle(ObjectFlag,ptext,paraNum,paragr)
    if location == 'object':
        ObjectFlag = 1
        comment_txt.write("object\n")
        comment_txt.write("��ʽ��ȷ������ΪͼƬ\n")
        continue
    rp.write(str(paraNum)+' '+ptext+' ' + location + '\n')
    first_text = 0
    if location != 'taskbook' and (ptext.startswith(" ") or ptext.startswith("��")):
        rp.write("    �����пո�\n")
        rp1.write(str(paraNum)+'_'+'paraStart'+'_error_'+'startWithSpace'+'_0\n')
    contain_ref(paragr,paraNum)                 
    
    if location in rules_dct.keys():
        rp.write('    λ�ã�'+rules_dct[location]['name']+'\n')
        errorInfo = check_out(rules_dct[location],p_format,location,paraNum,paragr)
    else:
        errorInfo=''
    if errorInfo :
        pass
    else:
        rp.write('    ��飺 ��ʽ��ȷ\n')
        if location not in['menuFirst','menuSecond','menuThird']:
            comment_txt.write('��飺��ʽ��ȷ\n')

for num in spaceNeeded:
    rp2.write('%d' %num)
    rp2.write('\n') 

endTime=time.time()
print '   ��ʱ�� %.2f ms' % (100*(endTime-startTime))


hyperlinks = []
bookmarks = []
#���Ŀ¼�Ƿ��Զ�����
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

rp.write('\n\n\n���ĸ�ʽ�����ϣ�\n')
rp.close()
rp1.close()
rp2.close()
comment_txt.close()
