# -*- mode: python ; coding: utf-8 -*-

block_cipher = None


a = Analysis(['mangle-infra-agent/mangleinfraagent/infra_agent.py'],
             pathex=['/root/gtm/mangle-infra-agent', '/root/gtm'],
             binaries=[],
             datas=[('/root/gtm/mangle-infra-agent/mangleinfraagent/logging.ini','.')],
             hiddenimports=['pymongo','cassandra.cluster','cassandra.auth','cassandra.encoder', 'concurrent', 'concurrent.futures', 'json', 'cassandra.connection', 'cassandra.marshal', 'cassandra.protocol', 'cassandra.type_codes', 'geomet.wkt', 'cassandra.compat', 'cassandra.cython_deps', 'cassandra.pool', 'cassandra.policies', 'cassandra.timestamps', 'cassandra.datastax', 'cassandra.datastax.insights', 'cassandra.datastax.insights.reporter', 'cassandra.datastax.cloud', 'cassandra.io', 'cassandra.io.asyncorereactor'],
             hookspath=[],
             runtime_hooks=[],
             excludes=[],
             win_no_prefer_redirects=False,
             win_private_assemblies=False,
             cipher=block_cipher,
             noarchive=False)
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          [],
          exclude_binaries=True,
          name='infra_agent',
          debug=False,
          bootloader_ignore_signals=False,
          strip=False,
          upx=True,
          console=True )
coll = COLLECT(exe,
               a.binaries,
               a.zipfiles,
               a.datas,
               strip=False,
               upx=True,
               upx_exclude=[],
               name='infra_agent')
