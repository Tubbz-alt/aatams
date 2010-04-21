<?xml version="1.0" encoding="UTF-8"?>
<!-- Created with Liquid XML Studio - FREE Community Edition 7.0.4.795 (http://www.liquid-technologies.com) -->
<!-- 
	Reverse the means of adding subfeatures.
	In this case subfeatures are added to their 'parent' feature by looking for foreign-keys on other tables that
	have the current table as thier 'foreingTable. This creates the xml hierarchy where a parent owns a set of 
	subfeatures of a particular type, for example:
	
	(a) personel involved in doing some task, the task may own some 'person-tasks', each owning some person.
	(b) a task involves making 0-N measurements, so each task owns 0 or more measurements subfeatures.
	
	Foreign-key fields within tables will either refer to a code table or non-code table. If the former we ideally
	want replace the primary-key integer with a 'code' string in the xml feature, however currently this is not
	possible in deegree 2.3 and we have to make it a sub-feature. 
	
	If the later we definitely want to embed the subfeature into the parent feature, either by selecting an 
	existing one or creating a new one. This is most likely to be the case of a 1-1 relationship but which sometimes
	might be 1-many, for example: 
	
	(a) a tag-release has a child tag but potentially a tag might be recovered and released again, so the db relation
	is tag has 0-N tag_releases.  However, tag_release is the primary feature and tags the secondary we aren't going
	to be interested presenting via XML tag_releases within tags.
	
	So for the time being these  
-->

<xsl:stylesheet version="2.0" xmlns="http://www.w3.org/1999/xhtml"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:aatams="http://www.imos.org.au/aatams"
	xmlns:xf="http://www.w3.org/2002/xforms"
	xmlns:ev="http://www.w3.org/2001/xml-events"
	xmlns:wfs="http://www.opengis.net/wfs"
	xmlns:ogc="http://www.opengis.net/ogc"
	xmlns:ows="http://www.opengis.net/ows"
	xmlns:gml="http://www.opengis.net/gml">
	<xsl:output name="xml" method="xml" encoding="UTF-8" indent="yes" />
	<xsl:include href="help.xslt" />
	<xsl:variable name="wfs_url">
		<xsl:text>../../deegree-wfs/services</xsl:text>
	</xsl:variable>
	<xsl:variable name="get_feature_url">
		<xsl:text>../../deegree-wfs/services?service=WFS&amp;version=1.1.0&amp;request=GetFeature&amp;namespace=xmlns(aatams=http://www.imos.org.au/aatams)&amp;typename=</xsl:text>
	</xsl:variable>
	<!-- add a variable to limit number of recursions when adding subfeatures -->
	<xsl:variable name="max_depth">5</xsl:variable>
	<xsl:variable name="namespace">aatams:</xsl:variable>
	<xsl:template match="/">
		<xsl:for-each select="//table">
			<xsl:variable name="feature-name">
				<xsl:value-of select="lower-case(@name)" />
			</xsl:variable>
			<xsl:result-document
				href="{concat('file:///C:/eclipse_workspace/aatams/forms5/create_',lower-case(@name),'.xml')}"
				format="xml">
				<xsl:processing-instruction name="xml-stylesheet">
					<xsl:text>href="xsltforms/xsltforms.xsl" type="text/xsl"</xsl:text>
				</xsl:processing-instruction>
				<html>
					<head>
						<title>
							<xsl:text>Australian Acoustic Tagging and Monitoring </xsl:text>
							<xsl:text>System (AATAMS)</xsl:text>
						</title>
						<link href="aatams.css" rel="stylesheet"
							type="text/css" />
						<xsl:call-template name="model" />
					</head>
					<body>
						<xsl:call-template name="form" />
					</body>
				</html>
			</xsl:result-document>
		</xsl:for-each>
	</xsl:template>

	<!-- 
		template to build the model 
	-->
	<xsl:template name="model">
		<xf:model id="model1">
			<!-- add the transaction instance (sent to wfs) -->
			<xsl:call-template name="wfs-transaction" />
			<!-- add the subfeature lists needed for select1 controls -->
			<xsl:call-template name="wfs-response" />
			<!-- add bindings for submission success or failure messages -->
			<xsl:call-template name="subfeature-lists" />
			<!-- add the model node bindings -->
			<xsl:call-template name="bindings" />
			<!-- add an instance to receive server response -->
			<xsl:call-template name="messages" />
			<!-- add the subfeature prototypes >
				<xsl:call-template name="prototypes" /-->
			<!-- add the xform submission details -->
			<xsl:call-template name="submission" />
			<!-- action to select first subfeatures in lists -->
			<xsl:call-template name="initialise-subfeatures" />
		</xf:model>
	</xsl:template>

	<!--
		template to create xml base transaction for submission to WFS 
	-->
	<xsl:template name="wfs-transaction">
		<xf:instance id="wfs-t">
			<wfs:Transaction version="1.1.0" service="WFS">
				<wfs:Insert>
					<wfs:FeatureCollection>
						<gml:featureMember>
							<xsl:call-template name="model-feature">
								<xsl:with-param name="table" select="." />
								<xsl:with-param name="depth"
									select="number(1)" />
							</xsl:call-template>
						</gml:featureMember>
					</wfs:FeatureCollection>
				</wfs:Insert>
			</wfs:Transaction>
		</xf:instance>
	</xsl:template>

	<!-- 
		recursive routine to add features (and subfeatures) to model
	-->
	<xsl:template name="model-feature">
		<xsl:param name="parent_table_name" />
		<xsl:param name="table" />
		<xsl:param name="depth" />
		<!--xsl:comment>
			<xsl:value-of select="concat('feature:',$depth)"/>
			</xsl:comment-->
		<xsl:choose>
			<xsl:when test="$depth > $max_depth">
				<xsl:element
					name="{concat($namespace,lower-case($table/@name))}">
					<xsl:if test="$depth > 1">
						<xsl:attribute name="gml:id" />
					</xsl:if>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="aatams:{lower-case($table/@name)}">
					<xsl:attribute name="gml:id" />
					<!-- handle each column -->
					<xsl:for-each select="$table/column">
						<xsl:choose>
							<!-- is it a foreign-key -->
							<xsl:when
								test="../foreign-key[reference/@local=current()/@name]">
								<xsl:variable name="foreign_table_name"
									select="../foreign-key[reference/@local=current()/@name][1]/@foreignTable" />
								<xsl:choose>
									<xsl:when
										test="$foreign_table_name = $parent_table_name">
										<!-- do nothing as we've just come from there  -->
									</xsl:when>
									<xsl:otherwise>
										<xsl:element
											name="{concat($namespace, lower-case($foreign_table_name), '_ref')}">
											<xsl:call-template
												name="model-subfeature">
												<xsl:with-param
													name="parent_table_name" select="$table/@name" />
												<xsl:with-param
													name="table_name" select="$foreign_table_name" />
												<xsl:with-param
													name="depth" select="$depth + 1" />
											</xsl:call-template>
										</xsl:element>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:when
								test="@primaryKey='true' and count(../column[@primaryKey='true'])=1 and
							@type='INTEGER'">
								<!-- exclude simple numeric primary keys a present in @gml:id -->
							</xsl:when>
							<xsl:otherwise>
								<!-- simple non-foreign-key -->
								<xsl:element
									name="aatams:{lower-case(@name)}" />
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<!-- add maxOccurs="unbounded" subfeatures by looking for foreign keys on other tables -->
					<!-- not interested if a code table -->
					<xsl:if
						test="$table/@codeTable = 'false' and $depth &lt; $max_depth">
						<xsl:for-each
							select="/database/table[not(@name=$parent_table_name) and foreign-key/@foreignTable=$table/@name and not(@codeTable='true')]">
							<xsl:element
								name="{concat($namespace, lower-case(./@name), '_ref')}">
								<xsl:call-template
									name="model-feature">
									<xsl:with-param
										name="parent_table_name" select="$table/@name" />
									<xsl:with-param name="table"
										select="." />
									<xsl:with-param name="depth"
										select="$max_depth" />
								</xsl:call-template>
							</xsl:element>
						</xsl:for-each>
					</xsl:if>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- 
		template for foreign-key columns/properties.
		we need to differentiate between two types of foreign keys:
		(1) one referencing a code table (or view) - add a subfeature
		(2) one referencing a non-code table (or view) - add the subfeature
		and it can be manually removed if not needed) 
	-->
	<xsl:template name="model-subfeature">
		<xsl:param name="parent_table_name" />
		<xsl:param name="table_name" />
		<xsl:param name="depth" />
		<xsl:choose>
			<xsl:when
				test="/database/table[@name=$table_name and @codeTable='true']">
				<xsl:call-template name="model-code-subfeature">
					<xsl:with-param name="table"
						select="/database/table[@name=$table_name and @codeTable='true'][1]" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when
				test="/database/view[@name=$table_name and @codeTable='true']">
				<xsl:call-template name="model-code-subfeature">
					<xsl:with-param name="table"
						select="/database/view[@name=$table_name and @codeTable='true'][1]" />
				</xsl:call-template>
			</xsl:when>
			<!-- not a code table so handle as for normal feature -->
			<xsl:when test="/database/table[@name=$table_name]">
				<xsl:call-template name="model-feature">
					<xsl:with-param name="parent_table_name"
						select="$parent_table_name" />
					<xsl:with-param name="table"
						select="/database/table[@name=$table_name][1]" />
					<xsl:with-param name="depth" select="$depth" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="model-feature">
					<xsl:with-param name="parent_table_name"
						select="$parent_table_name" />
					<xsl:with-param name="table"
						select="/database/view[@name=$table_name][1]" />
					<xsl:with-param name="depth" select="$depth" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
		recursive routine to add 'code' sub-features 
		where are only interested in distinguishing columns or subfeatures
	-->
	<xsl:template name="model-code-subfeature">
		<xsl:param name="table" />
		<xsl:element name="aatams:{lower-case($table/@name)}">
			<xsl:attribute name="gml:id" />
			<!-- handle each column -->
			<xsl:for-each select="$table/column">
				<!-- is it a distinguishing column -->
				<xsl:if
					test="$table/unique[@name='UNIQUE_INDEX']/column[@name=current()/@name]">
					<xsl:choose>
						<!-- if a foreign-key we will only allow one level of distinguishing subfeatures
							so depth is $max_depth -->
						<xsl:when
							test="../foreign-key[reference/@local=current()/@name]">
							<xsl:variable name="foreign_table_name"
								select="../foreign-key[reference/@local=current()/@name][1]/@foreignTable" />
							<xsl:element
								name="{concat($namespace, lower-case($foreign_table_name), '_ref')}">
								<xsl:call-template
									name="model-subfeature">
									<xsl:with-param name="table_name"
										select="$foreign_table_name" />
									<xsl:with-param name="depth"
										select="$max_depth" />
								</xsl:call-template>
							</xsl:element>
						</xsl:when>
						<!-- exclude simple numeric primary keys a present in @gml:id -->
						<xsl:when
							test="@primaryKey='true' and count(../column[@primaryKey='true'])=1 and
							@type='INTEGER'">
						</xsl:when>
						<!-- simple non-foreign-key -->
						<xsl:otherwise>
							<xsl:element
								name="{concat($namespace,lower-case(@name))}" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<!-- 
		template to create instances containing lists of subfeatures obtained from WFS 	
	-->
	<xsl:template name="subfeature-lists">
		<xsl:apply-templates select="foreign-key" mode="wfs-t" />
	</xsl:template>

	<!-- 
		template to create subfeature list instance from foreign-key
	-->
	<xsl:template match="foreign-key" mode="wfs-t">
		<xf:instance>
			<xsl:attribute name="id">
				<xsl:value-of select="lower-case(@foreignTable)" />
			</xsl:attribute>
			<xsl:attribute name="src">
				<xsl:value-of select="$wfs_url" /><xsl:text>?service=WFS&amp;version=1.1.0&amp;request=GetFeature&amp;namespace=xmlns(aatams=http://www.imos.org.au/aatams)&amp;typename=aatams:</xsl:text>
				<xsl:value-of select="lower-case(@foreignTable)" />
			</xsl:attribute>
		</xf:instance>
	</xsl:template>

	<!-- 
		template for adding prototype subfeatures.
		any foreign keys mean that this feature (potentially) has
		subfeature children, so create a prototype for adding a new child of
		a particular subfeature type.
	-->
	<xsl:template name="prototypes">
		<xf:instance id="prototypes">
			<xsl:for-each
				select="../table[foreign-key/@foreignTable=current()/@name]">
				<xsl:element
					name="aatams:{concat($namespace,lower-case(@name))}">
					<xsl:apply-templates select="column"
						mode="prototype" />
				</xsl:element>
			</xsl:for-each>
		</xf:instance>
	</xsl:template>

	<xsl:template match="column" mode="prototype">
		<!--xsl:choose>
			<xsl:when test=""></xsl:when>
			<xsl:otherwise></xsl:otherwise>
			</xsl:choose-->
	</xsl:template>

	<xsl:template match="table" mode="prototype">
		<xsl:apply-templates select="column" mode="prototype" />
	</xsl:template>

	<!-- 
		template for subfeature prototypes
	-->
	<xsl:template
		match="column[../foreign-key[reference/@local=current()/@name]]"
		mode="prototype" priority="3">
		<xsl:variable name="foreignTable">
			<xsl:value-of
				select="lower-case(../foreign-key[reference/@local=current()/@name][1]/@foreignTable)" />
		</xsl:variable>
		<xsl:element name="aatams:{concat($foreignTable,'_ref')}">

		</xsl:element>
	</xsl:template>

	<!--
		template to create data bindings
	-->
	<xsl:template name="bindings">
		<xsl:call-template name="binding-feature">
			<xsl:with-param name="path"
				select="concat(&quot;instance('wfs-t')&quot;,'/wfs:Insert/wfs:FeatureCollection/gml:featureMember/',$namespace,lower-case(@name))" />
			<xsl:with-param name="table" select="." />
			<xsl:with-param name="parent_id" select="''" />
			<xsl:with-param name="depth" select="number(1)" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="binding-feature">
		<xsl:param name="parent_table_name" />
		<xsl:param name="path" />
		<xsl:param name="table" />
		<xsl:param name="parent_id" />
		<xsl:param name="depth" />
		<xsl:if test="$depth &lt;= $max_depth">
			<xsl:variable name="id_root">
				<xsl:choose>
					<xsl:when test="string-length($parent_id) = 0">
						<xsl:text></xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat($parent_id,'_')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:for-each select="$table/column">
				<xsl:choose>
					<!--  subfeature as indicated by foreign-key -->
					<xsl:when
						test="../foreign-key[reference/@local=current()/@name]">
						<xsl:variable name="foreign_table_name"
							select="../foreign-key[reference/@local=current()/@name][1]/@foreignTable" />
						<xsl:choose>
							<xsl:when
								test="$foreign_table_name = $parent_table_name">
								<!-- do nothing as we've just come from there!  -->
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template
									name="binding-subfeature">
									<xsl:with-param
										name="parent_table_name" select="$table/@name" />
									<xsl:with-param name="path"
										select="concat($path,'/',$namespace,lower-case($foreign_table_name),'_ref')" />
									<xsl:with-param name="table_name"
										select="$foreign_table_name" />
									<xsl:with-param name="parent_id"
										select="concat($id_root,lower-case($foreign_table_name))" />
									<xsl:with-param name="depth"
										select="$depth+1" />
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<!-- primary-key -->
					<xsl:when
						test="@primaryKey='true' and count(../column[@primaryKey='true'])=1 and
							@type='INTEGER'">
						<!-- exclude simple numeric primary keys as present in @gml:id -->
					</xsl:when>
					<!-- simple property -->
					<xsl:otherwise>
						<xsl:element name="xf:bind">
							<xsl:attribute name="id">
								<xsl:value-of
									select="concat($id_root,lower-case(@name))" />
							</xsl:attribute>
							<xsl:attribute name="nodeset">
								<xsl:value-of
									select="concat($path,'/',$namespace,lower-case(@name))" />
							</xsl:attribute>
							<xsl:attribute name="required">
								<xsl:apply-templates select="@required" />
							</xsl:attribute>
							<xsl:attribute name="type">
								<xsl:apply-templates select="@type" />
							</xsl:attribute>
						</xsl:element>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<!-- differentiates between tables (code and non-code) and views -->
	<xsl:template name="binding-subfeature">
		<xsl:param name="parent_table_name" />
		<xsl:param name="path" />
		<xsl:param name="table_name" />
		<xsl:param name="parent_id" />
		<xsl:param name="depth" />
		<xsl:choose>
			<xsl:when
				test="/database/table[@name=$table_name and @codeTable='true']">
				<xsl:call-template name="binding-code-subfeature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case($table_name))" />
					<xsl:with-param name="table"
						select="/database/table[@name=$table_name and @codeTable='true'][1]" />
					<xsl:with-param name="parent_id"
						select="$parent_id" />
				</xsl:call-template>
			</xsl:when>
			<xsl:when
				test="/database/view[@name=$table_name and @codeTable='true']">
				<xsl:call-template name="binding-code-subfeature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case($table_name))" />
					<xsl:with-param name="table"
						select="/database/view[@name=$table_name and @codeTable='true'][1]" />
					<xsl:with-param name="parent_id"
						select="$parent_id" />
				</xsl:call-template>
			</xsl:when>
			<!-- not a code table so handle as for normal feature -->
			<xsl:when test="/database/table[@name=$table_name]">
				<xsl:call-template name="binding-feature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case($table_name))" />
					<xsl:with-param name="table"
						select="/database/table[@name=$table_name]" />
					<xsl:with-param name="parent_id"
						select="$parent_id" />
					<xsl:with-param name="depth" select="$depth+1" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="binding-feature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case($table_name))" />
					<xsl:with-param name="table"
						select="/database/view[@name=$table_name]" />
					<xsl:with-param name="parent_id"
						select="$parent_id" />
					<xsl:with-param name="depth" select="$depth+1" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>



	<!--
		recursive routine to add 'code' sub-features 
		we are only interested in distinguishing columns or subfeatures
	-->
	<xsl:template name="binding-code-subfeature">
		<xsl:param name="path" />
		<xsl:param name="table" />
		<xsl:param name="parent_id" />
		<xsl:variable name="id_root">
			<xsl:choose>
				<xsl:when test="string-length($parent_id) = 0">
					<xsl:text></xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat($parent_id,'_')" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!-- handle each column -->
		<xsl:for-each select="$table/column">
			<!-- is it a distinguishing column -->
			<xsl:if
				test="$table/unique[@name='UNIQUE_INDEX']/column[@name=current()/@name]">
				<xsl:choose>
					<!-- is it a foreign-key we will only allow one level of distinguishing subfeatures
						so depth is $max_depth -->
					<xsl:when
						test="../foreign-key[reference/@local=current()/@name]">
						<xsl:variable name="foreign_table_name"
							select="../foreign-key[reference/@local=current()/@name][1]/@foreignTable" />
						<xsl:call-template name="binding-subfeature">
							<xsl:with-param name="parent_table_name"
								select="$table/@name" />
							<xsl:with-param name="path"
								select="concat($path,'/',$namespace,lower-case($table/@name),'_ref')" />
							<xsl:with-param name="table_name"
								select="$foreign_table_name" />
							<xsl:with-param name="parent_id"
								select="concat($id_root,lower-case($table/@name))" />
							<xsl:with-param name="depth"
								select="$max_depth" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when
						test="@primaryKey='true' and count(../column[@primaryKey='true'])=1 and
							@type='INTEGER'">
						<!-- exclude simple numeric primary keys a present in @gml:id -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:element name="xf:bind">
							<xsl:attribute name="id">
							    <xsl:value-of
									select="concat($id_root,lower-case(@name))" />
						    </xsl:attribute>
							<xsl:attribute name="nodeset">
							    <xsl:value-of
									select="concat($path,'/',$namespace,lower-case(@name))" />
							</xsl:attribute>
							<xsl:attribute name="required">
								<xsl:apply-templates select="@required" />
							</xsl:attribute>
							<xsl:attribute name="type">
								<xsl:apply-templates select="@type" />
							</xsl:attribute>
						</xsl:element>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<!--
		converts db type to xsd type 
	-->
	<xsl:template match="@type">
		<xsl:choose>
			<xsl:when test=". = 'INTEGER'">
				<xsl:text>xsd:integer</xsl:text>
			</xsl:when>
			<xsl:when test=". = 'DECIMAL'">
				<xsl:text>xsd:decimal</xsl:text>
			</xsl:when>
			<xsl:when test=". = 'VARCHAR'">
				<xsl:text>xsd:string</xsl:text>
			</xsl:when>
			<xsl:when test=". = 'DATE'">
				<xsl:text>xsd:date</xsl:text>
			</xsl:when>
			<xsl:when test=". = 'TIMESTAMP'">
				<xsl:text>xsd:dateTime</xsl:text>
			</xsl:when>
			<xsl:when test=". = 'LONGVARCHAR'">
				<xsl:text>xsd:string</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>xsd:string</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="@required">
		<xsl:choose>
			<xsl:when test="current()='true'">
				<xsl:text>true()</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>false()</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- bindings for subfeatures -->
	<xsl:template
		match="column[../foreign-key[reference/@local=current()/@name]]"
		mode="binding" priority="3">
		<xsl:apply-templates
			select="../foreign-key[reference/@local=current()/@name]"
			mode="binding" />
	</xsl:template>

	<!-- 
		bindings for subfeature ids
	-->
	<xsl:template match="foreign-key" mode="binding">
		<xsl:element name="xf:bind">
			<xsl:attribute name="id">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="lower-case(@foreignTable)" />
			</xsl:attribute>
			<xsl:attribute name="nodeset">
				<xsl:text>instance('subf')/</xsl:text>
				<xsl:value-of select="lower-case(@foreignTable)" />
				<xsl:text>_id</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="type">xsd:string</xsl:attribute>
			<xsl:attribute name="required">
				<xsl:choose>
					<!-- see if the fk field is required="true"-->
					<xsl:when
						test="../column[@name=current()/reference/@local]/@required='true'">
						<xsl:text>true()</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>false()</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!--
		template to create form submission
	-->
	<xsl:template name="submission">
		<xf:submission id="s01" ref="instance('wfs-t')" method="post"
			action="{$wfs_url}" replace="instance" instance="resp">
			<!-- xf:action ev:event="xforms-submit">
				<xsl:apply-templates select="column" mode="submission" />
				<xsl:apply-templates select="foreign-key"
				mode="submission" />
				</xf:action-->
			<xf:message level="modeless"
				ev:event="xforms-submit-error">
				Submit error.
			</xf:message>
		</xf:submission>
	</xsl:template>

	<!--
		template to create submission response instance
	-->
	<xsl:template name="wfs-response">
		<xf:instance id="resp">
			<dummy xmlns="" />
		</xf:instance>
	</xsl:template>

	<!--
		template to create post submission messages
	-->
	<xsl:template name="messages">
		<xf:bind id="_error_message"
			nodeset="instance('resp')//ServiceException"
			calculate="choose(contains(.,'Equal feature'),substring-after(.,'. '),.)"
			type="xsd:string" />
		<xf:bind id="_success_message"
			nodeset="instance('resp')//ogc:FeatureId/@fid" type="xsd:string" />
	</xsl:template>

	<!-- 
		template to set initial 'selected' subfeature to be the first in list
	-->
	<xsl:template name="initialise-subfeatures">
		<xf:dispatch ev:event="xforms-ready" name="set-selected"
			target="model1" />
		<xf:action ev:event="set-selected">
			<xsl:for-each select="foreign-key">
				<xsl:call-template name="initialise-subfeature">
					<xsl:with-param name="path"
						select="concat(&quot;instance('wfs-t')&quot;,'/wfs:Insert/wfs:FeatureCollection/gml:featureMember/',$namespace,lower-case(../@name),'/',$namespace,lower-case(@foreignTable),'_ref')" />
					<xsl:with-param name="foreign-key" select="." />
					<xsl:with-param name="depth" select="number(1)" />
				</xsl:call-template>
			</xsl:for-each>
			<xf:dispatch name="xforms-revalidate" target="model1" />
		</xf:action>
	</xsl:template>

	<!--
		set @gml:id of subfeatures recursively
	-->
	<xsl:template name="initialise-subfeature">
		<xsl:param name="path" />
		<xsl:param name="foreign-key" />
		<xsl:param name="depth" />
		<xsl:if test="$depth &lt;= $max_depth">
			<xsl:variable name="foreign_table_name">
				<xsl:value-of select="$foreign-key/@foreignTable" />
			</xsl:variable>
			<!--  
				copy selected subfeature into submission
			-->
			<xsl:element name="xf:insert">
				<!-- where to put it -->
				<xsl:attribute name="nodeset">
						<xsl:value-of
						select="concat($path,'/',$namespace,lower-case($foreign_table_name))" />
					</xsl:attribute>
				<!-- what to put there -->
				<xsl:attribute name="origin">
						<xsl:value-of
						select="concat(&quot;instance('&quot;, lower-case($foreign_table_name), &quot;')/gml:featureMember/&quot;, $namespace, lower-case($foreign_table_name), '[1]')" />
					</xsl:attribute>
			</xsl:element>
			<!-- delete the dummy one -->
			<xsl:element name="xf:delete">
				<xsl:attribute name="nodeset">
						<xsl:value-of
						select="concat($path,'/',$namespace,lower-case($foreign_table_name))" />
					</xsl:attribute>
				<xsl:attribute name="at">1</xsl:attribute>
			</xsl:element>
			<!-- go to foreign table/view and look for more foreign-keys -->
			<xsl:for-each
				select="/database/table[@name=$foreign_table_name and @codeTable='true']/foreign-key">
				<xsl:call-template name="initialise-code-subfeature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case(./@foreignTable),'_ref')" />
					<xsl:with-param name="foreign-key" select="." />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each
				select="/database/view[@name=$foreign_table_name and @codeTable='true']/foreign-key">
				<xsl:call-template name="initialise-code-subfeature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case(./@foreignTable),'_ref')" />
					<xsl:with-param name="foreign-key" select="." />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each
				select="/database/table[@name=$foreign_table_name and @codeTable='false']/foreign-key">
				<xsl:call-template name="initialise-subfeature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case(./@foreignTable),'_ref')" />
					<xsl:with-param name="foreign-key" select="." />
					<xsl:with-param name="depth" select="$depth+1" />
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each
				select="/database/view[@name=$foreign_table_name and @codeTable='false']/foreign-key">
				<xsl:call-template name="initialise-subfeature">
					<xsl:with-param name="path"
						select="concat($path,'/',$namespace,lower-case(./@foreignTable),'_ref')" />
					<xsl:with-param name="foreign-key" select="." />
					<xsl:with-param name="depth" select="$depth+1" />
				</xsl:call-template>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	<!--
		set @gml:id of subfeatures recursively
	-->
	<xsl:template name="initialise-code-subfeature">
		<xsl:param name="path" />
		<xsl:param name="foreign-key" />
		<xsl:variable name="foreign_table_name">
			<xsl:value-of select="$foreign-key/@foreignTable" />
		</xsl:variable>
		<xsl:element name="xf:insert">
			<!-- where to put it -->
			<xsl:attribute name="nodeset">
						<xsl:value-of
					select="concat($path,'/',$namespace,lower-case($foreign_table_name))" />
					</xsl:attribute>
			<!-- what to put there -->
			<xsl:attribute name="origin">
						<xsl:value-of
					select="concat(&quot;instance('&quot;, lower-case($foreign_table_name), &quot;')/gml:featureMember/&quot;, $namespace, lower-case($foreign_table_name), '[1]')" />
					</xsl:attribute>
		</xsl:element>
		<!-- delete the dummy one -->
		<xsl:element name="xf:delete">
			<xsl:attribute name="nodeset">
						<xsl:value-of
					select="concat($path,'/',$namespace,lower-case($foreign_table_name))" />
					</xsl:attribute>
			<xsl:attribute name="at">1</xsl:attribute>
		</xsl:element>
	</xsl:template>
	<!--
		sets the initial selected value for select1 controls 
	-->
	<xsl:template match="foreign-key" mode="default-value">
		<xsl:element name="xf:setvalue">
			<xsl:variable name="feature">
				<xsl:value-of select="lower-case(@foreignTable)" />
			</xsl:variable>
			<xsl:attribute name="ref">
				<xsl:text>instance('subf')/</xsl:text>
				<xsl:value-of select="$feature" />
				<xsl:text>_id</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:text>instance('</xsl:text>
				<xsl:value-of select="$feature" />
				<xsl:text>')/gml:featureMember/aatams:</xsl:text>
				<xsl:value-of select="$feature" />
				<xsl:text>[1]/@gml:id</xsl:text>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- 
		template to build the form
	-->
	<xsl:template name="form">
		<div class="form">
			<label>
				<xsl:text>ADD </xsl:text>
				<xsl:value-of
					select="translate(upper-case(@name),'_',' ')" />
			</label>
			<div class="form-contents">
				<xf:switch>
					<xf:case id="{lower-case(@name)}" selected="true">
						<xsl:call-template name="form-feature">
							<xsl:with-param name="table" select="." />
							<xsl:with-param name="depth"
								select="number(1)" />
						</xsl:call-template>
						<xf:submit submission="s01">
							<xf:label>Save</xf:label>
						</xf:submit>
						<xf:trigger>
							<xf:label>Reset</xf:label>
							<xf:reset ev:event="DOMActivate" />
							<xf:dispatch ev:event="DOMActivate"
								name="set-selected" target="model1" />
							<xf:delete ev:event="DOMActivate"
								nodeset="instance('resp')//ServiceException" />
							<xf:delete ev:event="DOMActivate"
								nodeset="instance('resp')//ogc:FeatureId/@fid" />
						</xf:trigger>
						<div id="error-message">
							<xf:output bind="_error_message">
								<xf:label>Error:</xf:label>
							</xf:output>
						</div>
						<div id="success-message">
							<xf:output bind="_success_message">
								<xf:label>New Record Id:</xf:label>
							</xf:output>
						</div>
					</xf:case>
					<!-- add cases for subfeature prototype manipulation -->
					<xsl:for-each
						select="../table[foreign-key/@foreignTable=current()/@name]">
						<xf:case id="{lower-case(@name)}">
							<xsl:apply-templates select="."
								mode="form-case" />
						</xf:case>
					</xsl:for-each>
				</xf:switch>
			</div>
		</div>
	</xsl:template>

	<!--
		Creates an xform:group for the current table (feature) and processes the columns
	-->
	<xsl:template name="form-feature">
		<xsl:param name="table" />
		<xsl:param name="depth" />
		<xsl:choose>
			<xsl:when test="$depth > $max_depth">
				<!-- just add a select list for this feature -->
				<!--  xsl:element
					name="{concat($namespace,lower-case($table/@name))}">
					<xsl:attribute name="gml:id" />
					</xsl:element-->
			</xsl:when>
			<xsl:otherwise>
				<!--  add an xform group to reference the feature root node in model -->
				<xsl:element name="xf:group">
					<xsl:attribute name="ref">
						<xsl:choose>
							<xsl:when test="$depth = 1">
								<xsl:text>instance('wfs-t')/wfs:Insert/wfs:FeatureCollection/gml:featureMember/</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of
									select="concat($namespace,lower-case($table/@name),'_ref')" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:value-of
							select="concat($namespace,lower-case($table/@name))" />
					</xsl:attribute>
					<!-- handle each column -->
					<xsl:apply-templates select="column" mode="form" />
					<!-- add maxOccurs="unbounded" subfeatures by looking for foreign keys on other tables -->
					<!-- not interested if a code table -->
					<!-- xsl:if
						test="$table/@codeTable = 'false' and $depth &lt; $max_depth">
						<xsl:for-each
						select="/database/table[not(@name=$parent_table_name) and foreign-key/@foreignTable=$table/@name and not(@codeTable='true')]">
						<xsl:element
						name="{concat($namespace, lower-case(./@name), '_ref')}">
						<xsl:call-template
						name="model-feature">
						<xsl:with-param
						name="parent_table_name" select="$table/@name" />
						<xsl:with-param name="table"
						select="." />
						<xsl:with-param name="depth"
						select="$max_depth" />
						</xsl:call-template>
						</xsl:element>
						</xsl:for-each>
						</xsl:if-->
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--  
		NOTE: The following xform control generating templates are 
		overidable with more specific content templates by selecting
		on the column name.
	-->

	<!--  
		template to insert an xform input control
	-->
	<xsl:template match="column" mode="form">
		<xsl:element name="xf:input">
			<xsl:attribute name="ref">
					<xsl:value-of
					select="concat($namespace,lower-case(@name))" />
				</xsl:attribute>
			<xsl:attribute name="incremental">
					<xsl:text>true()</xsl:text>
				</xsl:attribute>
			<xf:label>
				<xsl:call-template name="proper-case">
					<xsl:with-param name="toconvert"
						select="replace(@name,'_',' ')" />
				</xsl:call-template>
			</xf:label>
			<xsl:call-template name="help">
				<xsl:with-param name="key">
					<xsl:value-of select="@name" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>

	<!--  
		template to insert an xform select1 control 
	-->
	<xsl:template
		match="column[../foreign-key[reference/@local=current()/@name]]"
		mode="form" priority="3">
		<xsl:call-template name="select1-from-foreign-key">
			<xsl:with-param name="fk_node"
				select="../foreign-key[reference/@local=current()/@name]" />
		</xsl:call-template>
	</xsl:template>

	<!-- 
		template to build an xforms select1 control
	-->
	<xsl:template name="select1-from-foreign-key">
		<xsl:param name="fk_node" />
		<!-- the feature name is the name of the foreign table -->
		<xsl:variable name="feature_name">
			<xsl:value-of select="lower-case($fk_node/@foreignTable)" />
		</xsl:variable>
		<xsl:element name="xf:select1">
			<xsl:attribute name="ref">
				<xsl:value-of
					select="concat($namespace,$feature_name,'_ref/',$namespace,$feature_name,'/@gml:id')" />
			</xsl:attribute>
			<xsl:attribute name="appearance">minimal</xsl:attribute>
			<xsl:attribute name="incremental">true()</xsl:attribute>
			<xf:label>
				<xsl:call-template name="proper-case">
					<xsl:with-param name="toconvert"
						select="translate($fk_node[1]/reference/@local,'_',' ')" />
				</xsl:call-template>
			</xf:label>
			<xsl:element name="xf:itemset">
				<xsl:attribute name="nodeset">
					<xsl:value-of
						select="concat(&quot;instance('&quot;, $feature_name, &quot;')/gml:featureMember/aatams:&quot;, $feature_name)" />
				</xsl:attribute>
				<xsl:element name="xf:value">
					<xsl:attribute name="ref">
						<xsl:text>@gml:id</xsl:text>
					</xsl:attribute>
				</xsl:element>
				<xsl:element name="xf:label">
					<xsl:attribute name="ref">
						<xsl:text>aatams:name</xsl:text>
					</xsl:attribute>
				</xsl:element>
			</xsl:element>
			<xsl:call-template name="help">
				<xsl:with-param name="key">
					<xsl:value-of select="@name" />
				</xsl:with-param>
			</xsl:call-template>
			<!-- when the selected value changes replace the relevant
				subfeature in the transaction by inserting a new one
				and then deleting the current one -->
			<xsl:element name="xf:action">
				<xsl:attribute name="ev:event">
					<xsl:text>xforms-value-changed</xsl:text>
				</xsl:attribute>
				<!--  
					copy selected subfeature into submission
				-->
				<xsl:element name="xf:insert">
					<!-- where to put it -->
					<xsl:attribute name="nodeset">
						<xsl:value-of select="'context()/../*'" />
					</xsl:attribute>
					<!-- what to put there -->
					<xsl:attribute name="origin">
						<xsl:value-of
							select="concat(&quot;instance('&quot;, $feature_name, &quot;')/gml:featureMember/&quot;, $feature_name, '[@gml:id=current()]')" />
					</xsl:attribute>
				</xsl:element>
				<!-- delete the dummy one -->
				<xsl:element name="xf:delete">
					<xsl:attribute name="nodeset">
						<xsl:value-of select="'context()/../*'" />
					</xsl:attribute>
					<xsl:attribute name="at">1</xsl:attribute>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>

	<!-- 
		template to convert control labels to proper-case
	-->
	<xsl:template name="proper-case">
		<xsl:param name="toconvert" />
		<xsl:if test="string-length($toconvert) &gt; 0">
			<xsl:variable name="f" select="substring($toconvert, 1, 1)" />
			<xsl:variable name="s" select="substring($toconvert, 2)" />
			<xsl:value-of select="upper-case($f)" />
			<xsl:choose>
				<xsl:when test="contains($s,' ')">
					<xsl:value-of
						select="lower-case(substring-before($s,' '))" />
					<xsl:text>&#160;</xsl:text>
					<xsl:call-template name="proper-case">
						<xsl:with-param name="toconvert"
							select="substring-after($s,' ')" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="lower-case($s)" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>


</xsl:stylesheet>
